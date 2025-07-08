package ch.frauenfelderflorian.bettersearch.models

import ch.frauenfelderflorian.bettersearch.services.searchEngineUuid
import java.util.UUID

data class SearchEngine(
  val id: UUID,
  val name: String,
  val suggestionUrl: String,
  val searchUrl: String,
)

val searchEngines = listOf(
  SearchEngine(
    id = searchEngineUuid(0),
    name = "DuckDuckGo",
    suggestionUrl = "https://ac.duckduckgo.com/ac/?type=list&q=",
    searchUrl = "https://duckduckgo.com/?q=",
  ),
  SearchEngine(
    id = searchEngineUuid(1),
    name = "Google",
    suggestionUrl = "https://suggestqueries.google.com/complete/search?client=firefox&q=",
    searchUrl = "https://www.google.com/search?q=",
  ),
  SearchEngine(
    id = searchEngineUuid(2),
    name = "Ecosia",
    suggestionUrl = "https://ac.ecosia.org/?type=list&q=",
    searchUrl = "https://www.ecosia.org/search?q=",
  ),
  SearchEngine(
    id = searchEngineUuid(3),
    name = "Brave",
    suggestionUrl = "https://search.brave.com/api/suggest?q=",
    searchUrl = "https://search.brave.com/search?q=",
  ),
  SearchEngine(
    id = searchEngineUuid(4),
    name = "Startpage",
    suggestionUrl = "https://www.startpage.com/osuggestions?q=",
    searchUrl = "https://www.startpage.com/sp/search?query=",
  ),
  SearchEngine(
    id = searchEngineUuid(5),
    name = "Swisscows",
    suggestionUrl = "https://api.swisscows.com/suggest?query=",
    searchUrl = "https://swisscows.com/web?query=",
  ),
  SearchEngine(
    id = searchEngineUuid(6),
    name = "Bing",
    suggestionUrl = "https://api.bing.com/osjson.aspx?query=",
    searchUrl = "https://www.bing.com/search?q=",
  ),
  SearchEngine(
    id = searchEngineUuid(7),
    name = "Yahoo",
    suggestionUrl = "https://search.yahoo.com/sugg/gossip/gossip-us-ura/?output=fxjson&command=",
    searchUrl = "https://search.yahoo.com/search?p=",
  ),
  SearchEngine(
    id = searchEngineUuid(8),
    name = "YouTube",
    suggestionUrl = "https://suggestqueries.google.com/complete/search?ds=yt&client=firefox&q=",
    searchUrl = "https://www.youtube.com/search?q=",
  ),
  SearchEngine(
    id = searchEngineUuid(9),
    name = "Wikipedia (English)",
    suggestionUrl = "https://wikipedia.org/w/api.php?action=opensearch&search=",
    // Maybe improve this as they provide links in the suggestions themselves
    searchUrl = "https://wikipedia.org/wiki/Special:Search?search=",
  ),
  SearchEngine(
    id = searchEngineUuid(10),
    name = "Reddit",
    suggestionUrl = "https://www.reddit.com/api/search_reddit_names.json?query=",
    searchUrl = "https://www.reddit.com/search/?q=",
  ),
)

fun UUID.getSearchEngine() = searchEngines.associateBy { it.id }[this]!!
