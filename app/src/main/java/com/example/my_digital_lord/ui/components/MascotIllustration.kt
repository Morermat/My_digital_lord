package com.example.my_digital_lord.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.example.my_digital_lord.R
import com.example.my_digital_lord.ui.theme.AppDimens

@Composable
fun MascotIllustration(
    mood: MascotMood = MascotMood.NEUTRAL,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppDimens.SmallRadius))
            .border(
                width = AppDimens.BorderWidth,
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(AppDimens.SmallRadius)
            )
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.lord_splash),
            contentDescription = "Господин",
            modifier = Modifier.fillMaxSize()
        )
    }
}