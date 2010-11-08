/*
 * Copyright (c) 2010.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; version 2 of the License.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 */

package com.btxtech.game.wicket.pages.mgmt.tutorial;

import com.btxtech.game.services.tutorial.DbItemSpeechBubbleHintConfig;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * User: beat
 * Date: 07.11.2010
 * Time: 15:28:19
 */
public class ItemSpeechBubbleHintConfigPanel extends Panel {
    public ItemSpeechBubbleHintConfigPanel(String id) {
        super(id);
        add(new TextField("syncItemId"));
        add(new TextArea("html"));
    }
}
