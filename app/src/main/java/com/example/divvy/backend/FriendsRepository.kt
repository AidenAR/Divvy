package com.example.divvy.backend

import com.example.divvy.components.GroupIcon
import com.example.divvy.models.Group
import com.example.divvy.models.ProfileRow
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject
import javax.inject.Singleton

data class FriendWithGroups(
    val profile: ProfileRow,
    val sharedGroups: List<Group>
)

interface FriendsRepository {
    suspend fun getFriendsWithGroups(): List<FriendWithGroups>
    suspend fun getProfileByPhone(phone: String): ProfileRow?
    suspend fun getProfileByEmail(email: String): ProfileRow?
}

@Serializable
private data class FriendGroupRow(
    @SerialName("user_id") val userId: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("email") val email: String? = null,
    @SerialName("phone") val phone: String? = null,
    @SerialName("group_id") val groupId: String,
    @SerialName("group_name") val groupName: String,
    @SerialName("group_icon") val groupIcon: String? = null
)

@Singleton
class SupabaseFriendsRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) : FriendsRepository {

    override suspend fun getFriendsWithGroups(): List<FriendWithGroups> {
        val rows = supabaseClient.postgrest
            .rpc("get_friends_with_groups")
            .decodeList<FriendGroupRow>()

        return rows.groupBy { it.userId }.map { (userId, userRows) ->
            val first = userRows.first()
            FriendWithGroups(
                profile = ProfileRow(
                    id = userId,
                    firstName = first.firstName,
                    lastName = first.lastName,
                    email = first.email,
                    phone = first.phone
                ),
                sharedGroups = userRows.map { row ->
                    Group(
                        id = row.groupId,
                        name = row.groupName,
                        icon = row.groupIcon?.let { iconName ->
                            runCatching { GroupIcon.valueOf(iconName) }.getOrDefault(GroupIcon.Group)
                        } ?: GroupIcon.Group
                    )
                }
            )
        }
    }

    override suspend fun getProfileByPhone(phone: String): ProfileRow? {
        return supabaseClient.from("profiles")
            .select {
                filter { eq("phone", phone) }
                limit(1)
            }
            .decodeSingleOrNull<ProfileRow>()
    }

    override suspend fun getProfileByEmail(email: String): ProfileRow? {
        return supabaseClient.from("profiles")
            .select {
                filter { eq("email", email) }
                limit(1)
            }
            .decodeSingleOrNull<ProfileRow>()
    }
}
