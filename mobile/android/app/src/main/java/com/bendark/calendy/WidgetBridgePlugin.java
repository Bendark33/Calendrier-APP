package com.bendark.calendy;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/** Pont entre l'appli web (JavaScript) et le widget : enregistre le résumé et rafraîchit le widget. */
@CapacitorPlugin(name = "WidgetBridge")
public class WidgetBridgePlugin extends Plugin {

    @PluginMethod
    public void update(PluginCall call) {
        String json = call.getString("json");
        if (json == null) {
            call.reject("json manquant");
            return;
        }
        CalendyWidgetData.save(getContext(), json);
        CalendyWidgetProvider.refreshAll(getContext());
        call.resolve();
    }

    /** À la déconnexion : on efface les données pour ne pas laisser d'événements sur l'écran d'accueil. */
    @PluginMethod
    public void clear(PluginCall call) {
        CalendyWidgetData.clear(getContext());
        CalendyWidgetProvider.refreshAll(getContext());
        call.resolve();
    }
}
