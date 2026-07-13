package com.moed.app.ui

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.moed.app.ui.components.MorphNavBar
import com.moed.app.ui.screens.CalendarScreen
import com.moed.app.ui.screens.ConverterScreen
import com.moed.app.ui.screens.PersonalScreen
import com.moed.app.ui.screens.SettingsScreen
import com.moed.app.ui.screens.TodayScreen
import com.moed.app.ui.screens.TzadikDetailScreen
import com.moed.app.ui.screens.TzadikimScreen
import com.moed.app.ui.screens.ZmanimCityScreen

/**
 * Table de routage figée (CONTRACTS §5.3).
 *
 * 5 destinations top-level (onglets de la [MorphNavBar]) + 3 sous-écrans atteints par argument.
 * Aucune route ne s'invente ni ne se renomme : ce sont les clés de navigation partagées avec les
 * widgets (deep-links) et les callbacks des écrans.
 */
object Routes {
    // --- Top-level (bottom nav, ordre spec today/calendar/personal/tsadikim/settings) ---
    const val TODAY = "today"
    const val CALENDAR = "calendar"
    const val PERSONAL = "personal"
    const val TZADIKIM = "tsadikim"
    const val SETTINGS = "settings"

    // --- Sous-écrans (arguments) ---
    const val TZADIK_DETAIL = "tsadikim/{slug}"
    const val ZMANIM_CITY = "zmanim/{citySlug}"
    const val CONVERTER = "converter"

    /** Onglets top-level, dans l'ordre d'affichage de la barre. */
    val topLevel = listOf(TODAY, CALENDAR, PERSONAL, TZADIKIM, SETTINGS)

    fun tzadik(slug: String) = "tsadikim/$slug"
    fun zmanimCity(slug: String) = "zmanim/$slug"

    /**
     * Ramène une route quelconque (y compris un sous-écran) sur l'onglet top-level qui doit rester
     * sélectionné dans la [MorphNavBar]. `zmanim/{citySlug}` est ouvert depuis Aujourd'hui,
     * `tsadikim/{slug}` depuis Tsadikim, `converter` depuis Réglages.
     */
    fun tabFor(route: String?): String = when {
        route == null -> TODAY
        route.startsWith("zmanim/") -> TODAY
        route.startsWith("tsadikim/") -> TZADIKIM
        route == CONVERTER -> SETTINGS
        route in topLevel -> route
        else -> TODAY
    }
}

/**
 * Chrome commun de l'application (CONTRACTS §5.3) : `Scaffold(bottomBar = MorphNavBar)` + `NavHost`
 * des 5 onglets et des 3 sous-écrans, le tout enveloppé d'un [CompositionLocalProvider] qui pilote
 * la direction de mise en page (RTL en hébreu, LTR sinon) d'après la langue effective du ViewModel.
 *
 * Un seul [MoedViewModel] est la source de vérité ; chaque écran le reçoit tel quel (signatures §5.4).
 * Une haptique « select » sèche est émise à chaque changement d'onglet.
 */
@Composable
fun MoedRoot() {
    val vm: MoedViewModel = viewModel()
    val lang by vm.lang.collectAsState()

    val direction = if (lang == "he") LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides direction) {
        val navController = rememberNavController()
        val view = LocalView.current

        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val currentTab = Routes.tabFor(currentRoute)

        Scaffold(
            bottomBar = {
                MorphNavBar(
                    current = currentTab,
                    onSelect = { target ->
                        if (target != currentTab) {
                            // Estampe tactile : retour sec, court (DESIGN §5).
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            navController.navigateTopLevel(target)
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.TODAY
            ) {
                // ---- 5 destinations top-level ----
                composable(Routes.TODAY) {
                    ScreenPadding(innerPadding) {
                        TodayScreen(
                            vm = vm,
                            onAllZmanim = { slug -> navController.navigate(Routes.zmanimCity(slug)) }
                        )
                    }
                }
                composable(Routes.CALENDAR) {
                    ScreenPadding(innerPadding) { CalendarScreen(vm = vm) }
                }
                composable(Routes.PERSONAL) {
                    ScreenPadding(innerPadding) { PersonalScreen(vm = vm) }
                }
                composable(Routes.TZADIKIM) {
                    ScreenPadding(innerPadding) {
                        TzadikimScreen(
                            vm = vm,
                            onOpenTzadik = { slug -> navController.navigate(Routes.tzadik(slug)) }
                        )
                    }
                }
                composable(Routes.SETTINGS) {
                    ScreenPadding(innerPadding) { SettingsScreen(vm = vm) }
                }

                // ---- 3 sous-écrans (arguments) ----
                composable(
                    route = Routes.TZADIK_DETAIL,
                    arguments = listOf(navArgument("slug") { type = NavType.StringType })
                ) { entry ->
                    val slug = entry.arguments?.getString("slug").orEmpty()
                    ScreenPadding(innerPadding) { TzadikDetailScreen(vm = vm, slug = slug) }
                }
                composable(
                    route = Routes.ZMANIM_CITY,
                    arguments = listOf(navArgument("citySlug") { type = NavType.StringType })
                ) { entry ->
                    val citySlug = entry.arguments?.getString("citySlug").orEmpty()
                    ScreenPadding(innerPadding) { ZmanimCityScreen(vm = vm, citySlug = citySlug) }
                }
                composable(Routes.CONVERTER) {
                    ScreenPadding(innerPadding) { ConverterScreen(vm = vm) }
                }
            }
        }
    }
}

/**
 * Applique le padding du [Scaffold] à un écran. Isolé pour garder chaque `composable` lisible et
 * éviter de répéter le `Modifier.padding(innerPadding)` sur chaque destination.
 */
@Composable
private fun ScreenPadding(
    innerPadding: PaddingValues,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
    ) { content() }
}

/**
 * Navigation d'onglet standard : ré-entrée single-top vers la destination de départ avec
 * sauvegarde/restauration d'état, pour éviter d'empiler des instances et conserver le scroll de
 * chaque onglet.
 */
private fun NavHostController.navigateTopLevel(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
