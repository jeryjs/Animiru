package eu.kanade.domain.items.episode.model

import eu.kanade.domain.entries.anime.model.downloadedFilter
import eu.kanade.tachiyomi.data.download.anime.AnimeDownloadManager
import eu.kanade.tachiyomi.ui.entries.anime.EpisodeList
import tachiyomi.domain.entries.anime.model.Anime
import tachiyomi.domain.entries.applyFilter
import tachiyomi.domain.items.episode.model.Episode
import tachiyomi.domain.items.episode.service.getEpisodeSort
import tachiyomi.source.local.entries.anime.isLocal

/**
 * Applies the view filters to the list of episodes obtained from the database.
 * @return an observable of the list of episodes filtered and sorted.
 */
fun List<Episode>.applyFilters(anime: Anime, downloadManager: AnimeDownloadManager): List<Episode> {
    val isLocalAnime = anime.isLocal()
    val unseenFilter = anime.unseenFilter
    val downloadedFilter = anime.downloadedFilter
    val bookmarkedFilter = anime.bookmarkedFilter
    // AM (FILLERMARK) -->
    val fillermarkedFilter = anime.fillermarkedFilter
    // <-- AM (FILLERMARK)

    return filter { episode -> applyFilter(unseenFilter) { !episode.seen } }
        .filter { episode -> applyFilter(bookmarkedFilter) { episode.bookmark } }
        // AM (FILLERMARK) -->
        .filter { episode -> applyFilter(fillermarkedFilter) { episode.fillermark } }
        // <-- AM (FILLERMARK)
        .filter { episode ->
            applyFilter(downloadedFilter) {
                val downloaded = downloadManager.isEpisodeDownloaded(
                    episode.name,
                    episode.scanlator,
                    // AM (CUSTOM_INFORMATION) -->
                    anime.ogTitle,
                    // <-- AM (CUSTOM_INFORMATION)
                    anime.source,
                )
                downloaded || isLocalAnime
            }
        }
        .sortedWith(getEpisodeSort(anime))
}

/**
 * Applies the view filters to the list of episodes obtained from the database.
 * @return an observable of the list of episodes filtered and sorted.
 */
fun List<EpisodeList.Item>.applyFilters(anime: Anime): Sequence<EpisodeList.Item> {
    val isLocalAnime = anime.isLocal()
    val unseenFilter = anime.unseenFilter
    val downloadedFilter = anime.downloadedFilter
    val bookmarkedFilter = anime.bookmarkedFilter
    // AM (FILLERMARK) -->
    val fillermarkedFilter = anime.fillermarkedFilter
    // <-- AM (FILLERMARK)
    return asSequence()
        .filter { (episode) -> applyFilter(unseenFilter) { !episode.seen } }
        .filter { (episode) -> applyFilter(bookmarkedFilter) { episode.bookmark } }
        // AM (FILLERMARK) -->
        .filter { (episode) -> applyFilter(fillermarkedFilter) { episode.fillermark } }
        // <-- AM (FILLERMARK)
        .filter { applyFilter(downloadedFilter) { it.isDownloaded || isLocalAnime } }
        .sortedWith { (episode1), (episode2) -> getEpisodeSort(anime).invoke(episode1, episode2) }
}
