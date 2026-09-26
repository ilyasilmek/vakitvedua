package com.stitchilyas.vakitvedua;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/** Web tarafı konum değiştirince widget'ı hemen yeniler. */
@CapacitorPlugin(name = "VakitWidget")
public class WidgetPlugin extends Plugin {
    @PluginMethod
    public void refresh(PluginCall call) {
        VakitWidget.updateAll(getContext());
        call.resolve();
    }
}
