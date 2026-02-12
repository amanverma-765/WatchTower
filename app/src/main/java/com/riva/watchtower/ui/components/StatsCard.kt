package com.riva.watchtower.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.riva.watchtower.ui.theme.BlueBackground
import com.riva.watchtower.ui.theme.BlueBorder
import com.riva.watchtower.ui.theme.BlueLabel
import com.riva.watchtower.ui.theme.BlueValue
import com.riva.watchtower.ui.theme.GreenBackground
import com.riva.watchtower.ui.theme.GreenBorder
import com.riva.watchtower.ui.theme.GreenLabel
import com.riva.watchtower.ui.theme.GreenValue
import com.riva.watchtower.ui.theme.OrangeBackground
import com.riva.watchtower.ui.theme.OrangeBorder
import com.riva.watchtower.ui.theme.OrangeLabel
import com.riva.watchtower.ui.theme.OrangeValue


@Composable
fun StatsCard(modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        // Total Sites - Blue tint
        Card(
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(.5.dp, BlueBorder),
            colors = CardDefaults.cardColors(
                containerColor = BlueBackground
            ),
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    "Total",
                    style = MaterialTheme.typography.labelMedium,
                    color = BlueLabel
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "20",
                    style = MaterialTheme.typography.headlineLarge,
                    color = BlueValue
                )
            }
        }

        // Changes Detected - Orange tint
        Card(
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(.5.dp, OrangeBorder),
            colors = CardDefaults.cardColors(
                containerColor = OrangeBackground
            ),
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    "Changes",
                    style = MaterialTheme.typography.labelMedium,
                    color = OrangeLabel
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "20",
                    style = MaterialTheme.typography.headlineLarge,
                    color = OrangeValue
                )
            }
        }

        // Passed Sites - Green tint
        Card(
            shape = MaterialTheme.shapes.medium,
            border = BorderStroke(.5.dp, GreenBorder),
            colors = CardDefaults.cardColors(
                containerColor = GreenBackground
            ),
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    "Passed",
                    style = MaterialTheme.typography.labelMedium,
                    color = GreenLabel
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "20",
                    style = MaterialTheme.typography.headlineLarge,
                    color = GreenValue
                )
            }
        }
    }
}