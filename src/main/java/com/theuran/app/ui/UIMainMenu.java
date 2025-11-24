package com.theuran.app.ui;

import com.theuran.app.App;
import mchorse.bbs.BBSSettings;
import mchorse.bbs.bridge.IBridge;
import mchorse.bbs.graphics.window.Window;
import mchorse.bbs.l10n.keys.IKey;
import mchorse.bbs.resources.Link;
import mchorse.bbs.ui.framework.UIBaseMenu;
import mchorse.bbs.ui.framework.UIRenderingContext;
import mchorse.bbs.ui.framework.elements.UIElement;
import mchorse.bbs.ui.framework.elements.buttons.UIButton;
import mchorse.bbs.ui.utils.UI;
import mchorse.bbs.utils.math.MathUtils;

public class UIMainMenu extends UIBaseMenu {
    private final UIButton button;
    private UIButton play;
    private UIButton openUpgrades;
    private UIButton quit;
    private UIButton cycleUIScale;
    private UIElement column;

    public UIMainMenu(IBridge bridge) {
        super(bridge);

        this.button = new UIButton(IKey.raw("Нажми меня"), (b) -> {
            System.out.println("Кнопка нажата!");
        });

        this.button.relative(this.main)
                .x(0.5F)
                .y(0.5F)
                .w(100)
                .h(20)
                .anchor(0.5F, 0.5F);

        this.main.add(this.button);
    }

    @Override
    public Link getMenuId() {
        return App.link("main");
    }

    @Override
    public void renderMenu(UIRenderingContext context, int mouseX, int mouseY) {
        context.batcher.box(this.main.area.x, this.main.area.y, this.main.area.ex(), this.main.area.ey(), 0xFF333333);

        super.renderMenu(context, mouseX, mouseY);
    }
}
