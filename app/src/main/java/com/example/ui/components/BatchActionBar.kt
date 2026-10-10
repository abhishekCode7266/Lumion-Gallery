package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BatchActionBar(
    selectedCount: Int,
    isTrashContext: Boolean = false,
    isVaultContext: Boolean = false,
    onCloseSelection: () -> Unit,
    onSelectAll: () -> Unit,
    onShare: () -> Unit,
    onFavorite: () -> Unit,
    onAddToAlbum: () -> Unit,
    onMoveToVault: () -> Unit,
    onDelete: () -> Unit,
    onRestore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onCloseSelection,
                        modifier = Modifier.testTag("batch_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close selection"
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$selectedCount selected",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = onSelectAll,
                    modifier = Modifier.testTag("batch_select_all_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Select All")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isTrashContext) {
                    BatchActionButton(
                        icon = Icons.Default.Restore,
                        label = "Restore",
                        onClick = onRestore,
                        tag = "batch_restore_button"
                    )
                    BatchActionButton(
                        icon = Icons.Default.Delete,
                        label = "Delete",
                        onClick = onDelete,
                        tag = "batch_permanent_delete_button"
                    )
                } else if (isVaultContext) {
                    BatchActionButton(
                        icon = Icons.Default.Restore,
                        label = "Unhide",
                        onClick = onRestore,
                        tag = "batch_unhide_button"
                    )
                    BatchActionButton(
                        icon = Icons.Default.Delete,
                        label = "Trash",
                        onClick = onDelete,
                        tag = "batch_trash_button"
                    )
                } else {
                    BatchActionButton(
                        icon = Icons.Default.Share,
                        label = "Share",
                        onClick = onShare,
                        tag = "batch_share_button"
                    )
                    BatchActionButton(
                        icon = Icons.Default.Favorite,
                        label = "Favorite",
                        onClick = onFavorite,
                        tag = "batch_favorite_button"
                    )
                    BatchActionButton(
                        icon = Icons.Default.CreateNewFolder,
                        label = "Album",
                        onClick = onAddToAlbum,
                        tag = "batch_album_button"
                    )
                    BatchActionButton(
                        icon = Icons.Default.Lock,
                        label = "Vault",
                        onClick = onMoveToVault,
                        tag = "batch_vault_button"
                    )
                    BatchActionButton(
                        icon = Icons.Default.Delete,
                        label = "Trash",
                        onClick = onDelete,
                        tag = "batch_delete_button"
                    )
                }
            }
        }
    }
}

@Composable
private fun BatchActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.testTag(tag)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
