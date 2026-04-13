package app.revanced.extension.studo;

import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Replaces the Go Pro dialog for calendar event color picking with the actual ColorPickerDialog.
 * Uses reflection because Studo classes are not available at extension compile time.
 */
public class CalendarColorPickerHelper {
    private static final String TAG = "CalendarColorPickerHelper";

    public static void showColorPicker(Object fragment) {
        try {
            // Get the current event color from CalendarAddFragment.color field
            Field colorField = findField(fragment.getClass(), "color");
            colorField.setAccessible(true);
            int currentColor = (int) colorField.get(fragment);

            // Get MbActivity from fragment
            Method getMbActivity = findMethod(fragment.getClass(), "getMbActivity");
            Object mbActivity = getMbActivity.invoke(fragment);
            if (mbActivity == null) return;

            // Get getSupportFragmentManager from the activity
            Method getSupportFragmentManager = findMethod(mbActivity.getClass(), "getSupportFragmentManager");
            Object fragmentManager = getSupportFragmentManager.invoke(mbActivity);
            if (fragmentManager == null) return;

            // Build ColorPickerDialog via reflection
            Class<?> cpDialogClass = Class.forName("com.jaredrummler.android.colorpicker.ColorPickerDialog");
            Method newBuilder = cpDialogClass.getMethod("newBuilder");
            Object builder = newBuilder.invoke(null);
            Class<?> builderClass = builder.getClass();

            builder = builderClass.getMethod("setDialogType", int.class).invoke(builder, 0);
            builder = builderClass.getMethod("setShowAlphaSlider", boolean.class).invoke(builder, false);
            builder = builderClass.getMethod("setAllowPresets", boolean.class).invoke(builder, false);
            builder = builderClass.getMethod("setColor", int.class).invoke(builder, currentColor);

            final Object dialog = builderClass.getMethod("create").invoke(builder);
            if (dialog == null) return;

            // Set ColorPickerDialogListener via dynamic Proxy
            Class<?> listenerInterface = Class.forName("com.jaredrummler.android.colorpicker.ColorPickerDialogListener");
            Object listener = Proxy.newProxyInstance(
                fragment.getClass().getClassLoader(),
                new Class[]{listenerInterface},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        if ("onColorSelected".equals(method.getName()) && args != null && args.length >= 2) {
                            try {
                                int selectedColor = (Integer) args[1];
                                String hexString = Integer.toHexString(selectedColor);
                                Method updateEventColor = findMethod(fragment.getClass(), "updateEventColor", String.class);
                                updateEventColor.setAccessible(true);
                                updateEventColor.invoke(fragment, hexString);
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to update event color", e);
                            }
                        }
                        return null;
                    }
                }
            );

            Method setListener = findMethod(dialog.getClass(), "setColorPickerDialogListener", listenerInterface);
            setListener.invoke(dialog, listener);

            dialog.getClass().getMethod("setRetainInstance", boolean.class).invoke(dialog, true);

            // showNow(FragmentManager, String)
            Method showNow = null;
            for (Method m : dialog.getClass().getMethods()) {
                if ("showNow".equals(m.getName()) && m.getParameterCount() == 2) {
                    showNow = m;
                    break;
                }
            }
            if (showNow != null) {
                showNow.invoke(dialog, fragmentManager, "color-picker-dialog");
            }

        } catch (Exception e) {
            Log.e(TAG, "Failed to show color picker", e);
        }
    }

    private static Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static Method findMethod(Class<?> clazz, String name, Class<?>... params) throws NoSuchMethodException {
        Class<?> c = clazz;
        while (c != null) {
            try {
                return c.getDeclaredMethod(name, params);
            } catch (NoSuchMethodException e) {
                c = c.getSuperclass();
            }
        }
        // Fall back to public inherited methods (e.g. getSupportFragmentManager)
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(name) && m.getParameterCount() == params.length) {
                return m;
            }
        }
        throw new NoSuchMethodException(name);
    }
}
