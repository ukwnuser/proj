package com.callback.app.ui.inbox

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.callback.app.R
import com.callback.app.data.Lead

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    newLeads: List<Lead>,
    doneLeads: List<Lead>,
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
                        LeadRow(lead = lead)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeadRow(lead: Lead) {
    Text(
        text = lead.displayName ?: lead.phoneNumber,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        style = MaterialTheme.typography.bodyLarge,
    )
}

/** Preview-friendly overload without Flow. */
@Composable
fun InboxScreenFromState(
    newLeadsFlow: kotlinx.coroutines.flow.Flow<List<Lead>>,
    doneLeadsFlow: kotlinx.coroutines.flow.Flow<List<Lead>>,
) {
    val newLeads by newLeadsFlow.collectAsState(initial = emptyList())
    val doneLeads by doneLeadsFlow.collectAsState(initial = emptyList())
    InboxScreen(newLeads = newLeads, doneLeads = doneLeads)
}
