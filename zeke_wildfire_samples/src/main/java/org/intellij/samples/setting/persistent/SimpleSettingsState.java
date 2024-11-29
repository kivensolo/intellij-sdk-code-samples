// Copyright 2000-2022 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

package org.intellij.samples.setting.persistent;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 支持以持久的方式存储应用程序设置。
 * {@link State}和{@link Storage}注解定义了数据的名称，以及存储这些持久化应用设置的文件名。
 * 类似于MVC模型的Model层。
 *
 * 储存的数据结构为：
 * <application>
 *   <component name="org.intellij.samples.persistent.SimpleSettingsState">
 *     <option name="ideaStatus" value="true" />
 *   </component>
 * </application>
 */
@State(
        name = "org.intellij.samples.persistent.SimpleSettingsState",
        storages = @Storage("SdkSettingsPlugin.xml")
)
public class SimpleSettingsState implements PersistentStateComponent<SimpleSettingsState> {

  public String userId = "PeterParker";
  public boolean ideaStatus = false;

  public static SimpleSettingsState getInstance() {
    return ApplicationManager.getApplication().getService(SimpleSettingsState.class);
  }

  @Nullable
  @Override
  public SimpleSettingsState getState() {
    return this;
  }

  @Override
  public void loadState(@NotNull SimpleSettingsState state) {
    XmlSerializerUtil.copyBean(state, this);
  }

}
