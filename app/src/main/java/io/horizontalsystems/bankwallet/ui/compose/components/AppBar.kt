package io.horizontalsystems.bankwallet.ui.compose.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import io.horizontalsystems.bankwallet.R
import io.horizontalsystems.bankwallet.ui.compose.ComposeAppTheme
import io.horizontalsystems.bankwallet.ui.compose.TranslatableString

data class MenuItem(
    val title: TranslatableString,
    @DrawableRes val icon: Int? = null,
    val enabled: Boolean = true,
    val tint: Color = Color.Unspecified,
    val showIconAndTitle: Boolean = false,
    val onClick: () -> Unit,
)

@Composable
fun AppBarMenuButton(
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    description: String? = null,
    enabled: Boolean = true,
    tint: Color = Color.Unspecified,
) {
    HsIconButton(
        onClick = onClick,
        enabled = enabled,
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = description,
            tint = tint
        )
    }
}

@Composable
fun AppBar(
    title: TranslatableString? = null,
    navigationIcon: @Composable (() -> Unit)? = null,
    menuItems: List<MenuItem> = listOf(),
    showSpinner: Boolean = false,
    backgroundColor: Color = ComposeAppTheme.colors.tyler
) {
    TopAppBar(
        modifier = Modifier.height(56.dp),
        title = {
            title?.let {
                title3_leah(
                    text = title.getString(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        backgroundColor = backgroundColor,
        navigationIcon = navigationIcon?.let {
            {
                navigationIcon()
            }
        },
        actions = {
            if (showSpinner) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(start = 24.dp, end = 16.dp)
                        .size(24.dp),
                    color = ComposeAppTheme.colors.grey,
                    strokeWidth = 2.dp
                )
            }
            menuItems.forEach { menuItem ->
                if (menuItem.showIconAndTitle && menuItem.icon != null) {
                    Button(
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color.Transparent,
                            contentColor = ComposeAppTheme.colors.leah,
                        ),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 0.dp,
                            pressedElevation = 0.dp,
                        ),
                        onClick = menuItem.onClick,
                    ) {
                        Row {
                            Icon(
                                painter = painterResource(id = menuItem.icon),
                                contentDescription = null,
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = menuItem.title.getString(),
                            )
                        }
                    }
                } else if (menuItem.icon != null) {
                    AppBarMenuButton(
                        icon = menuItem.icon,
                        onClick = menuItem.onClick,
                        enabled = menuItem.enabled,
                        tint = menuItem.tint,
                    )
                } else {
                    val color = if (menuItem.enabled) {
                        ComposeAppTheme.colors.jacob
                    } else {
                        ComposeAppTheme.colors.grey50
                    }

                    Text(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clickable(
                                enabled = menuItem.enabled,
                                onClick = menuItem.onClick
                            ),
                        text = menuItem.title.getString().toUpperCase(Locale.current),
                        style = ComposeAppTheme.typography.headline2,
                        color = color
                    )
                }
            }
        },
        elevation = 0.dp
    )
}