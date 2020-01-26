/*
  This is free and unencumbered software released into the public domain.

  Anyone is free to copy, modify, publish, use, compile, sell, or
  distribute this software, either in source code form or as a compiled
  binary, for any purpose, commercial or non-commercial, and by any
  means.

  In jurisdictions that recognize copyright laws, the author or authors
  of this software dedicate any and all copyright interest in the
  software to the public domain. We make this dedication for the benefit
  of the public at large and to the detriment of our heirs and
  successors. We intend this dedication to be an overt act of
  relinquishment in perpetuity of all present and future rights to this
  software under copyright law.

  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  OTHER DEALINGS IN THE SOFTWARE.

  For more information, please refer to <http://unlicense.org/>
 */
package io.onemfive.desktop;

import io.onemfive.desktop.views.View;
import io.onemfive.desktop.views.ViewPath;;
import io.onemfive.desktop.views.browser.BrowserView;
import io.onemfive.desktop.views.home.HomeView;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public final class Navigation {

    private static final ViewPath DEFAULT_VIEW_PATH = ViewPath.to(HomeView.class, BrowserView.class);

    public interface Listener {

        void onNavigationRequested(ViewPath path);

        default void onNavigationRequested(ViewPath path, Object data) {}
    }

    // New listeners can be added during iteration so we use CopyOnWriteArrayList to
    // prevent invalid array modification
    private final CopyOnWriteArraySet<Listener> listeners = new CopyOnWriteArraySet<>();
    private ViewPath currentPath;
    // Used for returning to the last important view. After setup is done we want to
    // return to the last opened view (e.g. sell/buy)
    private ViewPath returnPath;
    // this string is updated just before saving to disk so it reflects the latest currentPath situation.
//    private final NavigationPath navigationPath = new NavigationPath();

    // Persisted fields
    private ViewPath previousPath = DEFAULT_VIEW_PATH;

//    public Navigation(Storage<NavigationPath> storage) {
//        this.storage = storage;
//        storage.setNumMaxBackupFiles(3);
//    }

    public ViewPath getPreviousPath() {
        return previousPath;
    }

    public void setPreviousPath(ViewPath previousPath) {
        this.previousPath = previousPath;
    }

//    @Override
//    public void readPersisted() {
//        NavigationPath persisted = storage.initAndGetPersisted(navigationPath, "NavigationPath", 300);
//        if (persisted != null) {
//            List<Class<? extends View>> viewClasses = persisted.getPath().stream()
//                    .map(className -> {
//                        try {
//                            //noinspection unchecked
//                            return ((Class<? extends View>) Class.forName(className));
//                        } catch (ClassNotFoundException e) {
//                            log.warn("Could not find the viewPath class {}; exception: {}", className, e);
//                        }
//                        return null;
//                    })
//                    .filter(Objects::nonNull)
//                    .collect(Collectors.toList());
//
//            if (!viewClasses.isEmpty())
//                previousPath = new ViewPath(viewClasses);
//        }
//    }

    @SafeVarargs
    public final void navigateTo(Class<? extends View>... viewClasses) {
        navigateTo(ViewPath.to(viewClasses), null);
    }

    @SafeVarargs
    public final void navigateToWithData(Object data, Class<? extends View>... viewClasses) {
        navigateTo(ViewPath.to(viewClasses), data);
    }

    public void navigateTo(ViewPath newPath, Object data) {
        if (newPath == null)
            return;

        ArrayList<Class<? extends View>> temp = new ArrayList<>();
        for (int i = 0; i < newPath.size(); i++) {
            Class<? extends View> viewClass = newPath.get(i);
            temp.add(viewClass);
            if (currentPath == null ||
                    (currentPath != null &&
                            currentPath.size() > i &&
                            viewClass != currentPath.get(i) &&
                            i != newPath.size() - 1)) {
                ArrayList<Class<? extends View>> temp2 = new ArrayList<>(temp);
                for (int n = i + 1; n < newPath.size(); n++) {
                    //noinspection unchecked,unchecked,unchecked
                    Class<? extends View>[] newTemp = new Class[i + 1];
                    currentPath = ViewPath.to(temp2.toArray(newTemp));
                    navigateTo(currentPath, data);
                    viewClass = newPath.get(n);
                    temp2.add(viewClass);
                }
            }
        }

        currentPath = newPath;
        previousPath = currentPath;
//        queueUpForSave();
        listeners.forEach((e) -> e.onNavigationRequested(currentPath));
        listeners.forEach((e) -> e.onNavigationRequested(currentPath, data));
    }

//    private void queueUpForSave() {
//        if (currentPath.tip() != null) {
//            navigationPath.setPath(currentPath.stream().map(Class::getName).collect(Collectors.toList()));
//        }
//        storage.queueUpForSave(navigationPath, 1000);
//    }

    public void navigateToPreviousVisitedView() {
        if (previousPath == null || previousPath.size() == 0)
            previousPath = DEFAULT_VIEW_PATH;

        navigateTo(previousPath, null);
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public ViewPath getReturnPath() {
        return returnPath;
    }

    public ViewPath getCurrentPath() {
        return currentPath;
    }

    public void setReturnPath(ViewPath returnPath) {
        this.returnPath = returnPath;
    }
}
