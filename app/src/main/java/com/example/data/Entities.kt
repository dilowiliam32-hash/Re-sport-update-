package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_matches")
data class FavoriteMatch(
    @PrimaryKey val matchId: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeBadge: String,
    val awayBadge: String,
    val status: String,
    val matchTime: String,
    val competition: String
)

@Entity(tableName = "favorite_channels")
data class FavoriteChannel(
    @PrimaryKey val channelId: String,
    val title: String,
    val subtitle: String,
    val logoInitial: String,
    val isHd: Boolean
)

@Entity(tableName = "chat_comments")
data class ChatComment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val targetId: String, // e.g. "match_norway_vs_england" or "channel_dazn1"
    val username: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

// Domain Models (non-Room)
data class Match(
    val id: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeBadge: String,
    val awayBadge: String,
    val homeScore: String,
    val awayScore: String,
    val status: String, // Upcoming, Live, Finished
    val matchTime: String, // e.g., "10:00 PM"
    val matchDate: String, // e.g., "2026-07-11"
    val competition: String,
    val matchSummary: String = "",
    val timelineEvents: List<TimelineEvent> = emptyList(),
    val statistics: List<MatchStat> = emptyList(),
    val homeLineup: List<LineupPlayer> = emptyList(),
    val awayLineup: List<LineupPlayer> = emptyList()
)

data class TimelineEvent(
    val minute: String,
    val team: String, // "home" or "away"
    val playerName: String,
    val type: String, // "Goal", "Yellow Card", "Red Card", "Substitution"
    val detail: String = ""
)

data class MatchStat(
    val name: String,
    val homeValue: String,
    val awayValue: String,
    val homeProgress: Float, // 0.0 to 1.0
    val awayProgress: Float  // 0.0 to 1.0
)

data class LineupPlayer(
    val number: String,
    val name: String,
    val position: String
)

data class Channel(
    val id: String,
    val title: String,
    val subtitle: String,
    val logoInitial: String,
    val isHd: Boolean = true,
    val language: String = "English",
    val streamUrl: String = ""
)

data class Highlight(
    val id: String,
    val title: String,
    val subtitle: String, // e.g. "FIFA World Cup"
    val date: String,
    val thumbnailUrl: String,
    val videoUrl: String = ""
)

data class StandingRow(
    val position: Int,
    val teamName: String,
    val teamBadge: String,
    val played: Int,
    val won: Int,
    val drawn: Int,
    val lost: Int,
    val goalsFor: Int,
    val goalsAgainst: Int,
    val points: Int
)

data class TournamentGroup(
    val groupName: String,
    val standings: List<StandingRow>
)
