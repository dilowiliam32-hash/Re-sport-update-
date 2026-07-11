package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ZeSportViewModel(private val repository: Repository) : ViewModel() {

    // Selected Date Filter (Default matches Ze Sport main page: 2026-07-11)
    private val _selectedDate = MutableStateFlow("2026-07-11")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    // Matches List
    private val _matches = MutableStateFlow<List<Match>>(repository.getMatches())
    val matches: StateFlow<List<Match>> = _matches.asStateFlow()

    // Filtered Matches based on selected Date
    val filteredMatches: StateFlow<List<Match>> = combine(_matches, _selectedDate) { list, date ->
        list.filter { it.matchDate == date }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Channels & Highlights
    val channels: StateFlow<List<Channel>> = MutableStateFlow(repository.getChannels()).asStateFlow()
    val highlights: StateFlow<List<Highlight>> = MutableStateFlow(repository.getHighlights()).asStateFlow()
    val competitions: StateFlow<List<String>> = MutableStateFlow(repository.getCompetitions()).asStateFlow()
    val worldCupGroups: StateFlow<List<TournamentGroup>> = MutableStateFlow(repository.getWorldCupStandings()).asStateFlow()

    // Stream resolution state mapping (ID -> ExtractedStreamResult)
    private val _resolvedStreams = MutableStateFlow<Map<String, ExtractedStreamResult>>(emptyMap())
    val resolvedStreams: StateFlow<Map<String, ExtractedStreamResult>> = _resolvedStreams.asStateFlow()

    private val _isResolvingStream = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val isResolvingStream: StateFlow<Map<String, Boolean>> = _isResolvingStream.asStateFlow()

    fun resolveStream(id: String, title: String, subtitle: String, type: String) {
        if (_isResolvingStream.value[id] == true) return // already loading

        viewModelScope.launch {
            _isResolvingStream.value = _isResolvingStream.value + (id to true)
            try {
                val result = GeminiStreamService.resolveStreamUrls(id, title, subtitle, type)
                _resolvedStreams.value = _resolvedStreams.value + (id to result)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isResolvingStream.value = _isResolvingStream.value + (id to false)
            }
        }
    }

    // Advance Match Simulation/State (e.g. for today's live match)
    fun advanceMatchStatus(matchId: String) {
        val currentMatches = _matches.value.toMutableList()
        val index = currentMatches.indexOfFirst { it.id == matchId }
        if (index != -1) {
            val match = currentMatches[index]
            val updatedMatch = when (match.status) {
                "Upcoming" -> {
                    match.copy(
                        status = "Live",
                        homeScore = "0",
                        awayScore = "0",
                        timelineEvents = listOf(
                            TimelineEvent("0'", "home", "Kickoff", "Substitution", "Match has started! The players are on the pitch."),
                            TimelineEvent("5'", "away", "Declan Rice", "Yellow Card", "Late sliding tackle on Martin Odegaard.")
                        ),
                        statistics = listOf(
                            MatchStat("Possession", "52%", "48%", 0.52f, 0.48f),
                            MatchStat("Shots on Target", "1", "0", 1f, 0f),
                            MatchStat("Fouls", "2", "3", 0.4f, 0.6f),
                            MatchStat("Corner Kicks", "1", "0", 1f, 0f)
                        )
                    )
                }
                "Live" -> {
                    val currentGoals = (match.homeScore.toIntOrNull() ?: 0) + (match.awayScore.toIntOrNull() ?: 0)
                    when (currentGoals) {
                        0 -> {
                            match.copy(
                                homeScore = "1",
                                timelineEvents = match.timelineEvents + listOf(
                                    TimelineEvent("24'", "home", "Erling Haaland", "Goal", "Assisted by Odegaard with a brilliant defense-splitting pass! Incredible clinical finish.")
                                ),
                                statistics = listOf(
                                    MatchStat("Possession", "45%", "55%", 0.45f, 0.55f),
                                    MatchStat("Shots on Target", "3", "2", 0.6f, 0.4f),
                                    MatchStat("Fouls", "5", "4", 0.55f, 0.45f),
                                    MatchStat("Corner Kicks", "2", "2", 0.5f, 0.5f)
                                )
                            )
                        }
                        1 -> {
                            match.copy(
                                awayScore = "1",
                                timelineEvents = match.timelineEvents + listOf(
                                    TimelineEvent("58'", "away", "Jude Bellingham", "Goal", "Stunning overhead kick from Bukayo Saka's lofted cross! Absolute class.")
                                ),
                                statistics = listOf(
                                    MatchStat("Possession", "48%", "52%", 0.48f, 0.52f),
                                    MatchStat("Shots on Target", "4", "5", 0.44f, 0.56f),
                                    MatchStat("Fouls", "7", "8", 0.47f, 0.53f),
                                    MatchStat("Corner Kicks", "3", "4", 0.43f, 0.57f)
                                )
                            )
                        }
                        2 -> {
                            match.copy(
                                homeScore = "2",
                                timelineEvents = match.timelineEvents + listOf(
                                    TimelineEvent("82'", "home", "Erling Haaland", "Goal", "Haaland brace! Powerful header from Alexander Sorloth's cross into the top corner!")
                                ),
                                statistics = listOf(
                                    MatchStat("Possession", "46%", "54%", 0.46f, 0.54f),
                                    MatchStat("Shots on Target", "6", "6", 0.5f, 0.5f),
                                    MatchStat("Fouls", "9", "11", 0.45f, 0.55f),
                                    MatchStat("Corner Kicks", "4", "5", 0.44f, 0.56f)
                                )
                            )
                        }
                        else -> {
                            match.copy(
                                status = "Finished",
                                timelineEvents = match.timelineEvents + listOf(
                                    TimelineEvent("90+4'", "home", "Full Time", "Substitution", "Referee blows the final whistle! Norway seals an epic 2-1 victory over England.")
                                )
                            )
                        }
                    }
                }
                else -> {
                    match.copy(
                        status = "Upcoming",
                        homeScore = "0",
                        awayScore = "0",
                        timelineEvents = listOf(
                            TimelineEvent("0'", "home", "Kickoff", "Substitution", "Match about to start")
                        ),
                        statistics = listOf(
                            MatchStat("Possession", "50%", "50%", 0.5f, 0.5f),
                            MatchStat("Shots on Target", "0", "0", 0f, 0f),
                            MatchStat("Fouls", "0", "0", 0f, 0f),
                            MatchStat("Corner Kicks", "0", "0", 0f, 0f)
                        )
                    )
                }
            }
            currentMatches[index] = updatedMatch
            _matches.value = currentMatches
        }
    }

    // Room Database Bookmarked Flows
    val favoriteMatches: StateFlow<List<FavoriteMatch>> = repository.favoriteMatches
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteChannels: StateFlow<List<FavoriteChannel>> = repository.favoriteChannels
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Active Stream Chat Target (e.g. "match_norway_vs_england" or "channel_sport_tv5")
    private val _activeChatTarget = MutableStateFlow("")
    val activeChatTarget: StateFlow<String> = _activeChatTarget.asStateFlow()

    // Reactive Live Chat Flow
    @OptIn(ExperimentalCoroutinesApi::class)
    val activeChatComments: StateFlow<List<ChatComment>> = _activeChatTarget
        .flatMapLatest { targetId ->
            if (targetId.isEmpty()) flowOf(emptyList())
            else repository.getCommentsForTarget(targetId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User Session Mock
    val currentUser = "SportsFan_99"

    init {
        // Preload some default matches for other dates too, just in case
        viewModelScope.launch {
            // Seed comments for standard rooms
            repository.seedDefaultCommentsIfEmpty("match_norway_vs_england")
            repository.seedDefaultCommentsIfEmpty("channel_sport_tv5")
            repository.seedDefaultCommentsIfEmpty("channel_dazn_1_live")
            repository.seedDefaultCommentsIfEmpty("channel_espn_live")
        }
    }

    fun updateDateFilter(date: String) {
        _selectedDate.value = date
    }

    // Toggle Bookmarking for Matches
    fun toggleMatchFavorite(match: Match) {
        viewModelScope.launch {
            val favoritesList = favoriteMatches.value
            val isFav = favoritesList.any { it.matchId == match.id }
            if (isFav) {
                repository.deleteFavoriteMatch(match.id)
            } else {
                repository.insertFavoriteMatch(
                    FavoriteMatch(
                        matchId = match.id,
                        homeTeam = match.homeTeam,
                        awayTeam = match.awayTeam,
                        homeBadge = match.homeBadge,
                        awayBadge = match.awayBadge,
                        status = match.status,
                        matchTime = match.matchTime,
                        competition = match.competition
                    )
                )
            }
        }
    }

    // Toggle Bookmarking for Channels
    fun toggleChannelFavorite(channel: Channel) {
        viewModelScope.launch {
            val favoritesList = favoriteChannels.value
            val isFav = favoritesList.any { it.channelId == channel.id }
            if (isFav) {
                repository.deleteFavoriteChannel(channel.id)
            } else {
                repository.insertFavoriteChannel(
                    FavoriteChannel(
                        channelId = channel.id,
                        title = channel.title,
                        subtitle = channel.subtitle,
                        logoInitial = channel.logoInitial,
                        isHd = channel.isHd
                    )
                )
            }
        }
    }

    // Live Streaming Chat target switching
    fun setActiveChatTarget(targetId: String) {
        _activeChatTarget.value = targetId
        viewModelScope.launch {
            repository.seedDefaultCommentsIfEmpty(targetId)
        }
    }

    // Post comment to Room
    fun postComment(message: String) {
        val target = _activeChatTarget.value
        if (target.isNotEmpty() && message.isNotBlank()) {
            viewModelScope.launch {
                repository.addComment(target, currentUser, message)
            }
        }
    }

    // Retrieve full Match details
    fun getMatchById(matchId: String): Match? {
        return repository.getMatches().find { it.id == matchId }
    }

    // Retrieve full Channel details
    fun getChannelById(channelId: String): Channel? {
        return repository.getChannels().find { it.id == channelId }
    }

    // Retrieve full Highlight details
    fun getHighlightById(highlightId: String): Highlight? {
        return repository.getHighlights().find { it.id == highlightId }
    }
}

class ZeSportViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ZeSportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ZeSportViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
