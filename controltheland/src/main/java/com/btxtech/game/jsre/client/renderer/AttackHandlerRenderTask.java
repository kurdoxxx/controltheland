package com.btxtech.game.jsre.client.renderer;

import com.btxtech.game.jsre.client.common.Rectangle;
import com.btxtech.game.jsre.client.effects.AttackVisualization;
import com.btxtech.game.jsre.client.effects.Detonation;
import com.btxtech.game.jsre.client.effects.MuzzleFlash;
import com.btxtech.game.jsre.client.effects.ProjectileVisualization;
import com.btxtech.game.jsre.common.gameengine.syncObjects.SyncItem;
import com.google.gwt.canvas.dom.client.Context2d;

import java.util.Collection;

/**
 * User: beat
 * Date: 31.07.12
 * Time: 13:45
 */
public class AttackHandlerRenderTask extends AbstractClipRenderTask {
    private Context2d context2d;

    public AttackHandlerRenderTask(Context2d context2d) {
        this.context2d = context2d;
    }

    @Override
    public void render(long timeStamp, Collection<SyncItem> itemsInView, Rectangle viewRect, Rectangle tileViewRect) {
        for (ProjectileVisualization projectileVisualization : AttackVisualization.getInstance().getProjectilesInRegion(timeStamp, viewRect)) {
            renderClip(context2d, projectileVisualization);
        }
        for (MuzzleFlash muzzleFlash : AttackVisualization.getInstance().getMuzzleFlashInRegion(timeStamp, viewRect)) {
            renderClip(context2d, muzzleFlash);
        }
        for (Detonation detonation : AttackVisualization.getInstance().getDetonationsInRegion(timeStamp, viewRect)) {
            renderClip(context2d, detonation);
        }
    }
}
