package com.nuvio.app.features.trakt

import com.nuvio.app.features.addons.RawHttpResponse
import com.nuvio.app.features.collection.CollectionSource
import com.nuvio.app.features.collection.TraktCollectionSourceType
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TraktAccountCatalogClientTest {
    @Test
    fun `recommendations select media endpoint and remain single page`() = runBlocking {
        val requests = mutableListOf<String>()
        val receivedHeaders = mutableListOf<Map<String, String>>()
        val client = client { url, headers ->
            requests += url
            receivedHeaders += headers
            response(
                url = url,
                body = """
                    [{
                      "title": "Arrival",
                      "year": 2016,
                      "ids": {"trakt": 1, "imdb": "tt2543164", "tmdb": 329865},
                      "overview": "Language changes everything.",
                      "released": "2016-11-11",
                      "rating": 8.1,
                      "genres": ["science-fiction"],
                      "images": {"poster": ["media.trakt.tv/arrival.jpg"]}
                    }]
                """.trimIndent(),
            )
        }

        val moviePage = client.resolve(
            source = accountSource(TraktCollectionSourceType.RECOMMENDATIONS, "movie"),
            page = 1,
            headers = headers(),
        )
        val secondPage = client.resolve(
            source = accountSource(TraktCollectionSourceType.RECOMMENDATIONS, "movie"),
            page = 2,
            headers = headers(),
        )

        assertEquals(1, requests.size)
        assertTrue(requests.single().contains("/recommendations/movies?"))
        assertTrue(requests.single().contains("ignore_collected=false"))
        assertEquals("Bearer test", receivedHeaders.single()["Authorization"])
        assertEquals("tt2543164", moviePage.items.single().id)
        assertEquals("movie", moviePage.items.single().type)
        assertEquals("https://media.trakt.tv/arrival.jpg", moviePage.items.single().poster)
        assertNull(moviePage.nextSkip)
        assertTrue(secondPage.items.isEmpty())
    }

    @Test
    fun `watchlist paginates and maps shows`() = runBlocking {
        val requests = mutableListOf<String>()
        val client = client { url, _ ->
            requests += url
            response(
                url = url,
                body = """
                    [{
                      "rank": 2,
                      "listed_at": "2026-09-17T10:00:00.000Z",
                      "type": "show",
                      "show": {
                        "title": "Severance",
                        "year": 2022,
                        "ids": {"trakt": 2, "imdb": "tt11280740"},
                        "first_aired": "2022-02-18T00:00:00.000Z",
                        "images": {"poster": ["https://img.example/severance.jpg"]}
                      }
                    }]
                """.trimIndent(),
                headers = mapOf(
                    "X-Pagination-Page" to "2",
                    "X-Pagination-Page-Count" to "3",
                ),
            )
        }

        val page = client.resolve(
            source = accountSource(TraktCollectionSourceType.WATCHLIST, "series"),
            page = 2,
            headers = headers(),
        )

        assertTrue(requests.single().contains("/sync/watchlist/shows?"))
        assertTrue(requests.single().contains("page=2"))
        assertEquals("series", page.items.single().type)
        assertEquals("tt11280740", page.items.single().id)
        assertEquals(3, page.nextSkip)
    }

    @Test
    fun `calendar groups duplicate episodes by show and keeps earliest airing`() = runBlocking {
        val client = client { url, _ ->
            response(
                url = url,
                body = """
                    [
                      {
                        "first_aired": "2026-09-18T20:00:00.000Z",
                        "episode": {"season": 2, "number": 4, "title": "Later"},
                        "show": {"title": "The Bear", "ids": {"trakt": 3, "imdb": "tt14452776"}}
                      },
                      {
                        "first_aired": "2026-09-18T18:00:00.000Z",
                        "episode": {"season": 2, "number": 3, "title": "Earlier"},
                        "show": {"title": "The Bear", "ids": {"trakt": 3, "imdb": "tt14452776"}}
                      }
                    ]
                """.trimIndent(),
            )
        }

        val page = client.resolve(
            source = accountSource(TraktCollectionSourceType.CALENDAR, "series").copy(calendarDays = 3),
            page = 1,
            headers = headers(),
        )

        assertEquals(1, page.items.size)
        assertEquals("2026-09-18T18:00:00.000Z", page.items.single().rawReleaseDate)
        assertTrue(page.items.single().description.orEmpty().contains("S2E3"))
    }

    @Test
    fun `up next and unwatched exclude dropped shows and use progress`() = runBlocking {
        val requested = mutableListOf<String>()
        val requestedLock = Mutex()
        val client = client { url, _ ->
            requestedLock.withLock { requested += url }
            when {
                "/sync/watched/shows" in url -> response(
                    url,
                    """
                        [
                          {
                            "last_watched_at": "2026-09-17T10:00:00.000Z",
                            "show": {"title": "Active", "ids": {"trakt": 10, "imdb": "tt10"}, "images": {"poster": ["https://img/active.jpg"]}}
                          },
                          {
                            "last_watched_at": "2026-09-18T10:00:00.000Z",
                            "show": {"title": "Dropped", "ids": {"trakt": 20, "imdb": "tt20"}}
                          },
                          {
                            "last_watched_at": "2026-09-16T10:00:00.000Z",
                            "show": {"title": "Broken progress", "ids": {"trakt": 30, "imdb": "tt30"}}
                          }
                        ]
                    """.trimIndent(),
                    headers = mapOf("X-Pagination-Page-Count" to "1"),
                )

                "/users/hidden/dropped" in url -> response(
                    url,
                    """[{"show":{"ids":{"trakt":20}}}]""",
                    headers = mapOf("X-Pagination-Page-Count" to "1"),
                )

                "/calendars/my/shows/" in url -> response(
                    url,
                    """[{"first_aired":"2026-09-18T08:00:00.000Z","episode":{"season":1,"number":3},"show":{"title":"Active","ids":{"trakt":10,"imdb":"tt10"}}}]""",
                )

                "/shows/10/progress/watched" in url -> response(
                    url,
                    """
                        {
                          "aired": 3,
                          "completed": 1,
                          "next_episode": {
                            "season": 1,
                            "number": 2,
                            "title": "Next",
                            "overview": "The next chapter.",
                            "first_aired": "2026-09-17T20:00:00.000Z",
                            "ids": {"trakt": 102}
                          }
                        }
                    """.trimIndent(),
                )

                "/shows/30/progress/watched" in url -> RawHttpResponse(
                    status = 503,
                    statusText = "Unavailable",
                    url = url,
                    body = "{}",
                    headers = emptyMap(),
                )

                else -> error("Unexpected request: $url")
            }
        }

        val upNext = client.resolve(
            source = accountSource(TraktCollectionSourceType.UP_NEXT, "series"),
            page = 1,
            headers = headers(),
        )
        val unwatched = client.resolve(
            source = accountSource(TraktCollectionSourceType.UNWATCHED, "series"),
            page = 1,
            headers = headers(),
        )

        assertEquals(listOf("Active"), upNext.items.map { it.name })
        assertTrue(upNext.items.single().description.orEmpty().contains("S1E2"))
        assertEquals(listOf("Active"), unwatched.items.map { it.name })
        assertEquals("2026-09-18T08:00:00.000Z", unwatched.items.single().rawReleaseDate)
        assertFalse(requested.any { "/shows/20/progress/watched" in it })
    }

    private fun accountSource(type: TraktCollectionSourceType, mediaType: String): CollectionSource =
        CollectionSource(
            provider = "trakt",
            traktSourceType = type.value,
            mediaType = mediaType,
        )

    private fun headers(): Map<String, String> = mapOf("Authorization" to "Bearer test")

    private fun client(handler: suspend (String, Map<String, String>) -> RawHttpResponse): TraktAccountCatalogClient =
        TraktAccountCatalogClient(
            engine = TraktAccountCatalogHttpEngine { url, headers -> handler(url, headers) },
            nowEpochMs = { 2_000_000_000_000L },
            localIsoDateAtEpochMs = { epoch ->
                if (epoch < 2_000_000_000_000L) "2026-08-19" else "2026-09-18"
            },
            parseIsoDateTimeToEpochMs = { value ->
                when {
                    value.startsWith("2026-09-18") -> 2_000_000_000_000L
                    value.startsWith("2026-09-17") -> 1_999_900_000_000L
                    else -> null
                }
            },
        )

    private fun response(
        url: String,
        body: String,
        headers: Map<String, String> = emptyMap(),
    ): RawHttpResponse = RawHttpResponse(
        status = 200,
        statusText = "OK",
        url = url,
        body = body,
        headers = headers,
    )
}
