package app.revanced.patches.studo.pro

import app.revanced.patcher.*
import app.revanced.patcher.patch.BytecodePatchContext

internal val BytecodePatchContext.settingsIsProPatch by gettingFirstMethodDeclaratively {
    definingClass("Lcom/moshbit/studo/app/Settings;")
    name("isPro")
    returnType("Z")
    parameterTypes()
}

internal val BytecodePatchContext.restaurantLikePatch by gettingFirstMethodDeclaratively {
    definingClass("Lcom/moshbit/studo/home/lunch/LunchAdapter\$RestaurantVH;")
    name($$"_init_$lambda$2")
    returnType("V")
    parameterTypes(
        "Lcom/moshbit/studo/home/lunch/LunchAdapter;",
        $$"Lcom/moshbit/studo/home/lunch/LunchAdapter$RestaurantVH;",
        "Landroid/view/View;",
    )
}

internal val BytecodePatchContext.sendColorForUniEventPatch by gettingFirstMethodDeclaratively {
    definingClass("Lcom/moshbit/studo/home/calendar/CalendarAddFragment;")
    name("sendColorForUniEventToBackendIfNecessary")
    returnType("V")
    parameterTypes("Lcom/moshbit/studo/db/CalendarEvent;")
}

internal val BytecodePatchContext.sendColorForExternalEventPatch by gettingFirstMethodDeclaratively {
    definingClass("Lcom/moshbit/studo/home/calendar/CalendarAddFragment;")
    name("sendColorForExternalCalendarEventToBackendIfNecessary")
    returnType("V")
    parameterTypes("Lcom/moshbit/studo/db/CalendarEvent;")
}