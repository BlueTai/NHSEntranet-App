package com.example.nhsentranet.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nhsentranet.model.LinkedChildDto
import com.example.nhsentranet.model.SidebarItem
import com.example.nhsentranet.model.UserRoles
import com.example.nhsentranet.model.UserSession
import com.example.nhsentranet.ui.screens.*
import com.example.nhsentranet.ui.theme.*
import kotlinx.coroutines.launch

val mainSidebarItems = listOf(
    SidebarItem("/Dashboard", "Dashboard", Icons.Outlined.GridView, listOf(UserRoles.Admin, UserRoles.Teacher, UserRoles.HeadOfDepartment, UserRoles.Parent, UserRoles.Student)),
    SidebarItem("/AdminTools", "Admin Tools", Icons.Outlined.Layers, listOf(UserRoles.Admin)),
    SidebarItem("/StaffDirectory", "Staff Directory", Icons.Outlined.Groups, listOf(UserRoles.Admin, UserRoles.HeadOfDepartment, UserRoles.Teacher)),
    SidebarItem("/Students", "Students", Icons.Outlined.PeopleOutline, listOf(UserRoles.Admin, UserRoles.HeadOfDepartment, UserRoles.Teacher)),
    SidebarItem("/ManageForms", "Manage Forms", Icons.AutoMirrored.Outlined.Assignment, listOf(UserRoles.Admin, UserRoles.HeadOfDepartment)),
    SidebarItem("/Notices", "Notices", Icons.Outlined.Notifications, listOf(UserRoles.Admin, UserRoles.Teacher, UserRoles.HeadOfDepartment, UserRoles.Parent, UserRoles.Student)),
    SidebarItem("/Academic", "Academic", Icons.Outlined.School, listOf(UserRoles.Admin, UserRoles.Teacher, UserRoles.HeadOfDepartment, UserRoles.Parent, UserRoles.Student)),
    SidebarItem("/Contacts", "Contacts", Icons.Outlined.ContactPage, listOf(UserRoles.Admin, UserRoles.Teacher, UserRoles.HeadOfDepartment, UserRoles.Parent, UserRoles.Student)),
    SidebarItem("/Forms", "Forms", Icons.Outlined.Description, listOf(UserRoles.Admin, UserRoles.Parent, UserRoles.Student)),
    SidebarItem("/Invoices", "Invoices", Icons.Outlined.CreditCard, listOf(UserRoles.Admin, UserRoles.HeadOfDepartment, UserRoles.Parent)),
    SidebarItem("/Gallery", "Gallery", Icons.Outlined.Image, listOf(UserRoles.Admin, UserRoles.Teacher, UserRoles.HeadOfDepartment, UserRoles.Parent, UserRoles.Student)),
    SidebarItem("/Maps", "Maps", Icons.Outlined.LocationOn, listOf(UserRoles.Admin, UserRoles.Teacher, UserRoles.HeadOfDepartment, UserRoles.Parent, UserRoles.Student)),
    SidebarItem("/Calendar", "Calendar", Icons.Outlined.CalendarToday, listOf(UserRoles.Admin, UserRoles.Teacher, UserRoles.HeadOfDepartment, UserRoles.Parent, UserRoles.Student))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebsiteMatchedPortal(session: UserSession, onLogout: () -> Unit) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentRoute by remember { mutableStateOf("/Dashboard") }
    var selectedChild by remember {
        mutableStateOf(session.linkedChildren.firstOrNull() ?: LinkedChildDto(1, "John Johnson", "Grade 10A"))
    }

    val visibleMenuItems = mainSidebarItems.filter { it.roles.contains(session.userRole) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                drawerShape = RoundedCornerShape(topEnd = 0.dp, bottomEnd = 0.dp),
                modifier = Modifier.width(300.dp)
            ) {
                NewtonHouseBrandHeader()
                HorizontalDivider(color = WebDivider, thickness = 1.dp)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    visibleMenuItems.forEach { item ->
                        WebsiteSidebarRow(
                            label = item.label,
                            icon = item.icon,
                            isSelected = currentRoute == item.route,
                            onClick = {
                                currentRoute = item.route
                                scope.launch { drawerState.close() }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "A C C O U N T",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = WebSlateIcon,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )

                    WebsiteSidebarRow(
                        label = "Settings",
                        icon = Icons.Outlined.WbSunny,
                        isSelected = currentRoute == "/Settings",
                        onClick = {
                            currentRoute = "/Settings"
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = visibleMenuItems.find { it.route == currentRoute }?.label
                                    ?: if (currentRoute == "/Settings") "Settings" else "Dashboard",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = WebCharcoalText
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open Menu", tint = WebTealBrand)
                        }
                    },
                    actions = {
                        var dropdownExpanded by remember { mutableStateOf(false) }

                        if (session.linkedChildren.size > 1) {
                            Box {
                                Surface(
                                    color = WebActiveItemBg,
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(1.dp, WebTealBrand),
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .clickable { dropdownExpanded = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Outlined.Face, contentDescription = null, tint = WebTealBrand, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${selectedChild.fullName} (${selectedChild.gradeName}) ▼",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = WebTealBrand
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = dropdownExpanded,
                                    onDismissRequest = { dropdownExpanded = false }
                                ) {
                                    session.linkedChildren.forEach { child ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    "${child.fullName} (${child.gradeName})",
                                                    fontSize = 12.sp,
                                                    fontWeight = if (child.studentId == selectedChild.studentId) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                selectedChild = child
                                                dropdownExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        } else {
                            Surface(
                                color = WebActiveItemBg,
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.padding(end = 12.dp)
                            ) {
                                Text(
                                    text = "${session.fullName} (${session.userRole})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = WebTealBrand,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = WebHeaderCyan)
                )
            },
            containerColor = WebPageBg
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (currentRoute) {
                    "/Dashboard" -> AzureDashboardView(session, selectedChild, onSelectChild = { selectedChild = it }, onNavigate = { currentRoute = it })
                    "/AdminTools" -> AzureAdminToolsView()
                    "/StaffDirectory" -> AzureStaffDirectoryView()
                    "/Students" -> AzureStudentsListView()
                    "/ManageForms" -> AzureFormsView(selectedChild, isAdminMode = true)
                    "/Notices" -> AzureNoticesView(session)
                    "/Academic" -> AzureAcademicView(selectedChild)
                    "/Contacts" -> AzureContactsView()
                    "/Forms" -> AzureFormsView(selectedChild, isAdminMode = false)
                    "/Invoices" -> AzureInvoicesView(session, selectedChild)
                    "/Gallery" -> AzureGalleryView()
                    "/Maps" -> AzureMapsView()
                    "/Calendar" -> AzureCalendarView(selectedChild)
                    "/Settings" -> AzureSettingsView(session, onLogout)
                }
            }
        }
    }
}
