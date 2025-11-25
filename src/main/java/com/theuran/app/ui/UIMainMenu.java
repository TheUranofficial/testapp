package com.theuran.app.ui;

import com.theuran.app.App;
import mchorse.bbs.bridge.IBridge;
import mchorse.bbs.graphics.window.Window;
import mchorse.bbs.l10n.keys.IKey;
import mchorse.bbs.resources.Link;
import mchorse.bbs.ui.framework.UIBaseMenu;
import mchorse.bbs.ui.framework.UIRenderingContext;
import mchorse.bbs.ui.framework.elements.UIElement;
import mchorse.bbs.ui.framework.elements.buttons.UIIcon;
import mchorse.bbs.ui.utils.icons.Icons;

public class UIMainMenu extends UIBaseMenu {
    public UIMainMenu(IBridge bridge) {
        super(bridge);

        UIElement fimoz = new UIElement();

        fimoz.grid(20)
                .width(20)
                .padding(20);

        fimoz.relative(this.main).full()
                .x(0.5f).y(0.5f)
                .anchor(0.5f);

        Icons.ICONS.forEach((id, icon) -> {
            UIIcon value = new UIIcon(icon, callback -> {
                Window.setClipboard(id);
            });

            value.tooltip(IKey.raw("Key id: " + id));

            fimoz.add(value);
        });

        this.main.add(fimoz);
    }

    @Override
    public Link getMenuId() {
        return App.link("main");
    }

    @Override
    protected void preRenderMenu(UIRenderingContext context) {
        super.preRenderMenu(context);

        this.renderDefaultBackground();
    }
}
