// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.samples.setting.persistent;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * 演示如何实现自定义设置面板，并且添加持久化数据的读取和设置。
 * 在Setting的Tools分组下。
 *
 * Configurable实现类为应用程序设置提供控制器功能。
 * 类似于MVC模式的Controller
 */
public class SimpleSettingsConfigurable implements Configurable {

  private SimpleSettingsComponent mySettingsComponent;

  // A default constructor with no arguments is required because this implementation
  // is registered in an applicationConfigurable EP

  @Nls(capitalization = Nls.Capitalization.Title)
  @Override
  public String getDisplayName() {
    return "SDK: Application Settings Example";
  }

  @Override
  public JComponent getPreferredFocusedComponent() {
    return mySettingsComponent.getPreferredFocusedComponent();
  }

  /**
   * 创建一个JComponenet的UI组件
   * @return 通常为JPanel
   */
  @Nullable
  @Override
  public JComponent createComponent() {
    mySettingsComponent = new SimpleSettingsComponent();
    return mySettingsComponent.getPanel();
  }

  @Override
  public boolean isModified() {
    SimpleSettingsState settings = SimpleSettingsState.getInstance();
    boolean modified = !mySettingsComponent.getUserNameText().equals(settings.userId);
    modified |= mySettingsComponent.getIdeaUserStatus() != settings.ideaStatus;
    return modified;
  }

  @Override
  public void apply() {
    SimpleSettingsState settings = SimpleSettingsState.getInstance();
    settings.userId = mySettingsComponent.getUserNameText();
    settings.ideaStatus = mySettingsComponent.getIdeaUserStatus();
  }

  /**
   * 初次load或reset时，从数据层获取数据到UI界面
   */
  @Override
  public void reset() {
    SimpleSettingsState settings = SimpleSettingsState.getInstance();
    mySettingsComponent.setUserNameText(settings.userId);
    mySettingsComponent.setIdeaUserStatus(settings.ideaStatus);
  }

  @Override
  public void disposeUIResources() {
    mySettingsComponent = null;
  }

}
