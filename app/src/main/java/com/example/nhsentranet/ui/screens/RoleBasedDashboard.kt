package com.example.nhsentranet.ui.screens

import androidx.compose.runtime.Composable
import com.example.nhsentranet.model.LinkedChildDto
import com.example.nhsentranet.model.PortalTile
import com.example.nhsentranet.model.UserSession

@Composable
fun RoleBasedDashboard(
    session: UserSession,
    selectedChild: LinkedChildDto,
    onSelectChild: (LinkedChildDto) -> Unit,
    sections: List<PortalTile>,
    onNavigate: (String) -> Unit
) {
    RoleBasedDashboardScreen(
        session = session,
        selectedChild = selectedChild,
        onSelectChild = onSelectChild,
        sections = sections,
        onNavigate = onNavigate
    )
}
