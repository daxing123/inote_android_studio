package org.dayup.inotes.utils;

import org.dayup.common.Log;
import org.dayup.inotes.INotesApplication;
import org.dayup.inotes.R;
import org.dayup.inotes.constants.Constants.Themes;

import android.app.Activity;

public class ThemeUtils {
    private INotesApplication application;

    public ThemeUtils(INotesApplication application) {
        this.application = application;
    }

    /**
     * Set the theme of the activity, according to the configuration.
     */
    public void onActivityCreateSetTheme(Activity activity) {
        if (application.isLightTheme()) {
            activity.setTheme(R.style.Theme_INotes_Light_CustomActionBar);
            //            activity.setTheme(R.style.AppBaseTheme);
        } else if (application.isBlackTheme()) {
            activity.setTheme(R.style.Theme_INotes_Dark_CustomActionBar);
            //            activity.setTheme(R.style.AppBaseTheme);

        }
    }

    /**
     * Set the theme of the activity, according to the configuration.
     */
    // public void onActivityCreateSetThemeNoBg4DefaultActionBar(Activity
    // activity) {
    // if (application.isLightTheme()) {
    // activity.setTheme(R.style.Theme_INotes_Light);
    //
    // } else if (application.isBlackTheme()) {
    // activity.setTheme(R.style.Theme_INotes_Dark_NoBackground);
    //
    // }
    // }
    public int getActionBarListSectionLabelText() {
        switch (application.getThemeType()) {
        case Themes.THEME_BLACK:
            return R.color.list_section_label_text_dark;
        case Themes.THEME_LIGHT:
            return R.color.list_section_label_text_light;
        default:
            return R.color.list_section_label_text_light;
        }
    }

    public int getGLargeTextColor() {
        switch (application.getThemeType()) {
        case Themes.THEME_BLACK:
            return R.color.g_listitem_LargeText_bl;
        case Themes.THEME_LIGHT:
            return R.color.g_listitem_LargeText_lt;
        default:
            return R.color.g_listitem_LargeText_lt;
        }
    }

    public int getActionBarListSectionDividerBg() {
        switch (application.getThemeType()) {
        case Themes.THEME_BLACK:
            return R.drawable.list_section_divider_holo_dark;
        case Themes.THEME_LIGHT:
        default:
            return R.drawable.list_section_divider_light;
        }
    }

    public int getItemSelector() {
        switch (application.getThemeType()) {
        case Themes.THEME_BLACK:
            //Log.d("x","--------------------black click--------------------");
            return R.drawable.item_press_dark;
        case Themes.THEME_LIGHT:
        default:
            //Log.d("x","--------------------white click--------------------");
            return R.drawable.item_press_white;
        }

        /*if (application.isLightTheme()) {
            return R.drawable.item_press_dark;

        }else {
            return R.drawable.item_press_dark;

        }*/
    }

    public int getItemSelectorPressed() {
        switch (application.getThemeType()) {
        case Themes.THEME_BLACK:
            //Log.d("x","--------------------black acclick--------------------");
            return R.drawable.item_press_in_actionmode_dark;
        case Themes.THEME_LIGHT:
        default:
            //Log.d("x","--------------------white acclick--------------------");
            return R.drawable.item_press_in_actionmode_white;
        }

        /*if (application.isLightTheme()) {
            return R.drawable.item_press_in_actionmode_white;
        }else {
            return R.drawable.item_press_in_actionmode_dark;

        }*/
    }

}
