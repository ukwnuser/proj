package com.callback.app.ui.inbox

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.callback.app.R
import com.callback.app.data.AppDatabase
import com.callback.app.data.Lead
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    newLeads: List<Lead>,
    doneLeads: List<Lead>,
    onMarkDone: (Lead) -> Unit,
    onDelete: (Lead) -> Unit,
    onCall: (Lead) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.tab_new),
        stringResource(R.string.tab_done),
    )
    val leads = if (selectedTab == 0) newLeads else doneLeads

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.inbox_title)) })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) },
                    )
                }
            }
            if (leads.isEmpty()) {
                Text(
                    text = stringResource(R.string.empty_inbox),
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(leads, key = { it.id }) { lead ->
                        LeadCard(
                            lead = lead,
                            onMarkDone = onMarkDone,
                            onDelete = onDelete,
                            onCall = onCall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LeadCard(
    lead: Lead,
    onMarkDone: (Lead) -> Unit,
    onDelete: (Lead) -> Unit,
    onCall: (Lead) -> Unit,
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = lead.displayName ?: lead.phoneNumber,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = lead.phoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Row {
                IconButton(onClick = { onCall(lead) }) {
                    Icon(Icons.Default.Call, contentDescription = "Call")
                }
                if (!lead.isDone) {
                    IconButton(onClick = { onMarkDone(lead) }) {
                        Icon(Icons.Default.Check, contentDescription = "Mark as done")
                    }
                }
                IconButton(onClick = { onDelete(lead) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

/** Preview-friendly overload without Flow. */
@Composable
fun InboxScreenFromState(
    newLeadsFlow: kotlinx.coroutines.flow.Flow<List<Lead>>,
    doneLeadsFlow: kotlinx.coroutines.flow.Flow<List<Lead>>,
    database: AppDatabase,
) {
    val newLeads by newLeadsFlow.collectAsState(initial = emptyList())
    val doneLeads by doneLeadsFlow.collectAsState(initial = emptyList())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    InboxScreen(
        newLeads = newLeads,
        doneLeads = doneLeads,
        onMarkDone = { lead ->
            scope.launch {
                database.leadDao().markAsDone(lead.id)
            }
        },
        onDelete = { lead ->
            scope.launch {
                database.leadDao().delete(lead.id)
            }
        },
        onCall = { lead ->
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${lead.phoneNumber}")
            }
            context.startActivity(intent)
        },
    )
}
