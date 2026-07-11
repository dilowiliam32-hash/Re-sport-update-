package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf

class Repository(private val db: AppDatabase) {

    // DAOs
    private val favoriteMatchDao = db.favoriteMatchDao()
    private val favoriteChannelDao = db.favoriteChannelDao()
    private val chatCommentDao = db.chatCommentDao()

    // Favorite Matches
    val favoriteMatches: Flow<List<FavoriteMatch>> = favoriteMatchDao.getAllFavoriteMatches()
    fun isMatchFavorite(matchId: String): Flow<Boolean> = favoriteMatchDao.isMatchFavorite(matchId)
    suspend fun insertFavoriteMatch(match: FavoriteMatch) = favoriteMatchDao.insertFavoriteMatch(match)
    suspend fun deleteFavoriteMatch(matchId: String) = favoriteMatchDao.deleteFavoriteMatch(matchId)

    // Favorite Channels
    val favoriteChannels: Flow<List<FavoriteChannel>> = favoriteChannelDao.getAllFavoriteChannels()
    fun isChannelFavorite(channelId: String): Flow<Boolean> = favoriteChannelDao.isChannelFavorite(channelId)
    suspend fun insertFavoriteChannel(channel: FavoriteChannel) = favoriteChannelDao.insertFavoriteChannel(channel)
    suspend fun deleteFavoriteChannel(channelId: String) = favoriteChannelDao.deleteFavoriteChannel(channelId)

    // Live Chats
    fun getCommentsForTarget(targetId: String): Flow<List<ChatComment>> = chatCommentDao.getCommentsForTarget(targetId)
    suspend fun addComment(targetId: String, username: String, message: String) {
        chatCommentDao.insertComment(ChatComment(targetId = targetId, username = username, message = message))
    }

    // Seed default comments if empty
    suspend fun seedDefaultCommentsIfEmpty(targetId: String) {
        val currentComments = chatCommentDao.getCommentsForTarget(targetId).firstOrNull()
        if (currentComments.isNullOrEmpty()) {
            val defaults = when {
                targetId.contains("norway") -> listOf(
                    "Sven" to "Let's go Norway! Haaland is going to score a hat-trick tonight!",
                    "Marcus" to "England looks strong, but we can beat them on our day.",
                    "LionsFan" to "It's coming home! England 3-0 easy win.",
                    "Arne" to "What a great atmosphere here at the World Cup match center!",
                    "Harry" to "Norway's defense has been shaky, Kane will exploit that.",
                    "Olav" to "Come on Sven, be realistic, a 1-1 draw would be amazing!"
                )
                targetId.contains("spain") -> listOf(
                    "Carlos" to "Spain vs Belgium was a masterclass by Nico Williams!",
                    "Romelu" to "Belgium played well but Spain's passing was just superior.",
                    "Elena" to "This highlight is amazing! What a finish by Olmo!"
                )
                targetId.contains("france") -> listOf(
                    "MbappeFan" to "France absolutely dominated Morocco. Deserved final spot!",
                    "Youssef" to "Morocco played with passion, so proud of the Atlas Lions anyway.",
                    "Pierre" to "Allez les Bleus! Solid 3-0."
                )
                else -> listOf(
                    "Streamer99" to "Is this link working smoothly for everyone? Looks great here!",
                    "Admin" to "Welcome to the Ze Sport live feed! Enjoy the match.",
                    "GamerX" to "1080p stream is buttery smooth, thanks Ze Sport team!"
                )
            }
            defaults.forEach { (user, msg) ->
                addComment(targetId, user, msg)
            }
        }
    }

    // Site Data Mock Sources (Predefined static content)
    fun getMatches(): List<Match> = listOf(
        Match(
            id = "norway_vs_england",
            homeTeam = "Norway",
            awayTeam = "England",
            homeBadge = "https://r2.thesportsdb.com/images/media/team/badge/gyfn811591973155.png",
            awayBadge = "https://r2.thesportsdb.com/images/media/team/badge/vf5ttc1726166739.png",
            homeScore = "0",
            awayScore = "0",
            status = "Upcoming",
            matchTime = "10:00 PM",
            matchDate = "2026-07-11",
            competition = "FIFA World Cup",
            matchSummary = "FIFA World Cup 2026 Group Stage match at the majestic MetLife Stadium. A clash of superstars - Erling Haaland leads Norway's attack against Harry Kane's England. Expected attendance is 82,500.",
            timelineEvents = listOf(
                TimelineEvent("0'", "home", "Kickoff", "Substitution", "Match about to start")
            ),
            statistics = listOf(
                MatchStat("Possession", "50%", "50%", 0.5f, 0.5f),
                MatchStat("Shots on Target", "0", "0", 0.0f, 0.0f),
                MatchStat("Fouls", "0", "0", 0.0f, 0.0f),
                MatchStat("Corner Kicks", "0", "0", 0.0f, 0.0f)
            ),
            homeLineup = listOf(
                LineupPlayer("1", "Nyland", "GK"),
                LineupPlayer("3", "Ajer", "DF"),
                LineupPlayer("4", "Ostigard", "DF"),
                LineupPlayer("5", "Meling", "DF"),
                LineupPlayer("10", "Odegaard", "MF"),
                LineupPlayer("15", "Berge", "MF"),
                LineupPlayer("16", "Aursnes", "MF"),
                LineupPlayer("9", "Haaland", "FW"),
                LineupPlayer("11", "Sorloth", "FW")
            ),
            awayLineup = listOf(
                LineupPlayer("1", "Pickford", "GK"),
                LineupPlayer("2", "Walker", "DF"),
                LineupPlayer("5", "Stones", "DF"),
                LineupPlayer("6", "Guehi", "DF"),
                LineupPlayer("3", "Shaw", "DF"),
                LineupPlayer("4", "Rice", "MF"),
                LineupPlayer("8", "Bellingham", "MF"),
                LineupPlayer("11", "Foden", "MF"),
                LineupPlayer("7", "Saka", "FW"),
                LineupPlayer("9", "Kane", "FW"),
                LineupPlayer("10", "Palmer", "FW")
            )
        ),
        Match(
            id = "spain_vs_belgium",
            homeTeam = "Spain",
            awayTeam = "Belgium",
            homeBadge = "https://r2.thesportsdb.com/images/media/team/badge/g89y1n1519730591.png",
            awayBadge = "https://r2.thesportsdb.com/images/media/team/badge/vqqttr1519730514.png",
            homeScore = "2",
            awayScore = "1",
            status = "Finished",
            matchTime = "8:00 PM",
            matchDate = "2026-07-10",
            competition = "FIFA World Cup",
            matchSummary = "Spain seals a narrow victory over Belgium at the Mercedes-Benz Stadium. Lamine Yamal and Nico Williams were instrumental in breaking down the compact Belgian defense.",
            timelineEvents = listOf(
                TimelineEvent("18'", "home", "Nico Williams", "Goal", "Assisted by Yamal"),
                TimelineEvent("42'", "away", "Kevin De Bruyne", "Yellow Card", "Tactical Foul"),
                TimelineEvent("65'", "away", "Romelu Lukaku", "Goal", "Header from corner"),
                TimelineEvent("81'", "home", "Dani Olmo", "Goal", "Stunning volley outside box")
            ),
            statistics = listOf(
                MatchStat("Possession", "62%", "38%", 0.62f, 0.38f),
                MatchStat("Shots on Target", "6", "3", 0.66f, 0.33f),
                MatchStat("Fouls", "8", "14", 0.36f, 0.64f),
                MatchStat("Corner Kicks", "8", "4", 0.66f, 0.33f)
            ),
            homeLineup = listOf(
                LineupPlayer("1", "Raya", "GK"),
                LineupPlayer("2", "Carvajal", "DF"),
                LineupPlayer("3", "Le Normand", "DF"),
                LineupPlayer("14", "Laporte", "DF"),
                LineupPlayer("16", "Rodri", "MF"),
                LineupPlayer("10", "Olmo", "MF"),
                LineupPlayer("17", "Williams", "FW"),
                LineupPlayer("19", "Yamal", "FW")
            ),
            awayLineup = listOf(
                LineupPlayer("1", "Casteels", "GK"),
                LineupPlayer("2", "Castagne", "DF"),
                LineupPlayer("4", "Faes", "DF"),
                LineupPlayer("7", "De Bruyne", "MF"),
                LineupPlayer("10", "Doku", "MF"),
                LineupPlayer("9", "Lukaku", "FW")
            )
        ),
        Match(
            id = "france_vs_morocco",
            homeTeam = "France",
            awayTeam = "Morocco",
            homeBadge = "https://r2.thesportsdb.com/images/media/team/badge/7f3v3v1519730554.png",
            awayBadge = "https://r2.thesportsdb.com/images/media/team/badge/v6j3s31519730571.png",
            homeScore = "3",
            awayScore = "0",
            status = "Finished",
            matchTime = "6:00 PM",
            matchDate = "2026-07-09",
            competition = "FIFA World Cup",
            matchSummary = "A masterful display by the French team as they defeat Morocco at the NRG Stadium. Kylian Mbappé dazzled with two spectacular goals.",
            timelineEvents = listOf(
                TimelineEvent("12'", "home", "Kylian Mbappé", "Goal", "Individual run"),
                TimelineEvent("34'", "home", "Antoine Griezmann", "Goal", "Penalty"),
                TimelineEvent("78'", "home", "Kylian Mbappé", "Goal", "Assisted by Dembélé")
            ),
            statistics = listOf(
                MatchStat("Possession", "55%", "45%", 0.55f, 0.45f),
                MatchStat("Shots on Target", "8", "2", 0.8f, 0.2f),
                MatchStat("Fouls", "11", "12", 0.48f, 0.52f)
            )
        ),
        Match(
            id = "switzerland_vs_colombia",
            homeTeam = "Switzerland",
            awayTeam = "Colombia",
            homeBadge = "https://r2.thesportsdb.com/images/media/team/badge/vqvqtq1519730623.png",
            awayBadge = "https://r2.thesportsdb.com/images/media/team/badge/vququq1519730602.png",
            homeScore = "1",
            awayScore = "2",
            status = "Finished",
            matchTime = "4:00 PM",
            matchDate = "2026-07-07",
            competition = "FIFA World Cup",
            matchSummary = "Colombia edges Switzerland in a thrilling encounter. James Rodríguez delivered a match-winning performance with two assists.",
            timelineEvents = listOf(
                TimelineEvent("25'", "home", "Breel Embolo", "Goal", "Header"),
                TimelineEvent("49'", "away", "Luis Díaz", "Goal", "Tap-in"),
                TimelineEvent("77'", "away", "John Durán", "Goal", "Header")
            ),
            statistics = listOf(
                MatchStat("Possession", "44%", "56%", 0.44f, 0.56f),
                MatchStat("Shots on Target", "3", "5", 0.37f, 0.63f)
            )
        ),
        Match(
            id = "argentina_vs_egypt",
            homeTeam = "Argentina",
            awayTeam = "Egypt",
            homeBadge = "https://r2.thesportsdb.com/images/media/team/badge/vwtywx1519730534.png",
            awayBadge = "https://r2.thesportsdb.com/images/media/team/badge/vqqvqv1519730521.png",
            homeScore = "2",
            awayScore = "2",
            status = "Finished",
            matchTime = "9:00 PM",
            matchDate = "2026-07-07",
            competition = "FIFA World Cup",
            matchSummary = "A historic battle ending in 2-2. Argentina eventually triumphed on penalties (4-3) in front of an electric crowd.",
            timelineEvents = listOf(
                TimelineEvent("31'", "away", "Mostafa Mohamed", "Goal", "Counterattack"),
                TimelineEvent("55'", "home", "Lionel Messi", "Goal", "Free kick"),
                TimelineEvent("73'", "away", "Trezeguet", "Goal", "Deflection"),
                TimelineEvent("90'", "home", "Lautaro Martínez", "Goal", "Injury time header")
            ),
            statistics = listOf(
                MatchStat("Possession", "65%", "35%", 0.65f, 0.35f),
                MatchStat("Shots on Target", "7", "4", 0.63f, 0.37f)
            )
        ),
        Match(
            id = "usa_vs_belgium",
            homeTeam = "USA",
            awayTeam = "Belgium",
            homeBadge = "https://r2.thesportsdb.com/images/media/team/badge/gqqvty1519730612.png",
            awayBadge = "https://r2.thesportsdb.com/images/media/team/badge/vqqttr1519730514.png",
            homeScore = "0",
            awayScore = "1",
            status = "Finished",
            matchTime = "2:00 PM",
            matchDate = "2026-07-07",
            competition = "FIFA World Cup",
            matchSummary = "Belgium gets a hard-fought 1-0 victory over the tournament hosts USA. Lois Openda scored the decisive goal."
        ),
        Match(
            id = "portugal_vs_spain",
            homeTeam = "Portugal",
            awayTeam = "Spain",
            homeBadge = "https://r2.thesportsdb.com/images/media/team/badge/gqwxwy1519730582.png",
            awayBadge = "https://r2.thesportsdb.com/images/media/team/badge/g89y1n1519730591.png",
            homeScore = "1",
            awayScore = "3",
            status = "Finished",
            matchTime = "9:00 PM",
            matchDate = "2026-07-06",
            competition = "FIFA World Cup",
            matchSummary = "Spain triumphs 3-1 over neighboring Portugal in a dramatic Iberian Derby at the MetLife Stadium."
        )
    )

    fun getChannels(): List<Channel> = listOf(
        Channel(
            id = "sport_tv5",
            title = "Sport TV5 – World Cup",
            subtitle = "HD · English",
            logoInitial = "Sp"
        ),
        Channel(
            id = "dazn_1_live",
            title = "DAZN 1 Live",
            subtitle = "HD · English",
            logoInitial = "DA"
        ),
        Channel(
            id = "espn_live",
            title = "Watch ESPN live",
            subtitle = "HD · English",
            logoInitial = "Wa"
        )
    )

    fun getHighlights(): List<Highlight> = listOf(
        Highlight(
            id = "spain_vs_belgium_highlight",
            title = "Spain vs Belgium",
            subtitle = "FIFA World Cup",
            date = "2026-07-10",
            thumbnailUrl = "https://www.thesportsdb.com/images/media/event/thumb/e0awtb1783410723.jpg"
        ),
        Highlight(
            id = "france_vs_morocco_highlight",
            title = "France vs Morocco",
            subtitle = "FIFA World Cup",
            date = "2026-07-09",
            thumbnailUrl = "https://www.thesportsdb.com/images/media/event/thumb/7pkil31783237187.jpg"
        ),
        Highlight(
            id = "switzerland_vs_colombia_highlight",
            title = "Switzerland vs Colombia",
            subtitle = "FIFA World Cup",
            date = "2026-07-07",
            thumbnailUrl = "https://www.thesportsdb.com/images/media/event/thumb/2swkia1783166926.jpg"
        ),
        Highlight(
            id = "argentina_vs_egypt_highlight",
            title = "Argentina vs Egypt",
            subtitle = "FIFA World Cup",
            date = "2026-07-07",
            thumbnailUrl = "https://www.thesportsdb.com/images/media/event/thumb/08mivj1783166773.jpg"
        ),
        Highlight(
            id = "usa_vs_belgium_highlight",
            title = "USA vs Belgium",
            subtitle = "FIFA World Cup",
            date = "2026-07-07",
            thumbnailUrl = "https://www.thesportsdb.com/images/media/event/thumb/uccm5r1782983934.jpg"
        ),
        Highlight(
            id = "portugal_vs_spain_highlight",
            title = "Portugal vs Spain",
            subtitle = "FIFA World Cup",
            date = "2026-07-06",
            thumbnailUrl = "https://www.thesportsdb.com/images/media/event/thumb/xb1fyz1783166773.jpg"
        )
    )

    fun getCompetitions(): List<String> = listOf(
        "FIFA World Cup 2026",
        "UEFA Champions League",
        "English Premier League",
        "Spanish La Liga",
        "Italian Serie A",
        "German Bundesliga",
        "French Ligue 1",
        "Saudi Pro League",
        "Egyptian Premier League"
    )

    fun getWorldCupStandings(): List<TournamentGroup> = listOf(
        TournamentGroup(
            groupName = "Group A",
            standings = listOf(
                StandingRow(1, "Spain", "https://r2.thesportsdb.com/images/media/team/badge/g89y1n1519730591.png", 3, 3, 0, 0, 8, 2, 9),
                StandingRow(2, "Belgium", "https://r2.thesportsdb.com/images/media/team/badge/vqqttr1519730514.png", 3, 2, 0, 1, 5, 3, 6),
                StandingRow(3, "Egypt", "https://r2.thesportsdb.com/images/media/team/badge/vqqvqv1519730521.png", 3, 0, 1, 2, 3, 6, 1),
                StandingRow(4, "USA", "https://r2.thesportsdb.com/images/media/team/badge/gqqvty1519730612.png", 3, 0, 1, 2, 1, 6, 1)
            )
        ),
        TournamentGroup(
            groupName = "Group B",
            standings = listOf(
                StandingRow(1, "France", "https://r2.thesportsdb.com/images/media/team/badge/7f3v3v1519730554.png", 3, 2, 1, 0, 6, 1, 7),
                StandingRow(2, "Morocco", "https://r2.thesportsdb.com/images/media/team/badge/v6j3s31519730571.png", 3, 1, 2, 0, 4, 2, 5),
                StandingRow(3, "Switzerland", "https://r2.thesportsdb.com/images/media/team/badge/vqvqtq1519730623.png", 3, 1, 0, 2, 3, 5, 3),
                StandingRow(4, "Colombia", "https://r2.thesportsdb.com/images/media/team/badge/vququq1519730602.png", 3, 0, 1, 2, 2, 7, 1)
            )
        ),
        TournamentGroup(
            groupName = "Group C",
            standings = listOf(
                StandingRow(1, "England", "https://r2.thesportsdb.com/images/media/team/badge/vf5ttc1726166739.png", 0, 0, 0, 0, 0, 0, 0),
                StandingRow(2, "Norway", "https://r2.thesportsdb.com/images/media/team/badge/gyfn811591973155.png", 0, 0, 0, 0, 0, 0, 0)
            )
        )
    )
}
