/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.projectexplorer;

import com.google.inject.Inject;
import java.net.URL;
import org.eclipse.che.selenium.core.SeleniumWebDriver;
import org.eclipse.che.selenium.core.user.DefaultTestUser;
import org.eclipse.che.selenium.core.webdriver.UploadFileUtil;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** @author Dmytro Nochevnov */
public class UploadFileIntoProjectTest {
  private static final String TEST_PROJECT = "TestProject1";
  private static final URL PROJECT_SOURCES =
      DownloadProjectTest.class.getResource("/projects/ProjectWithDifferentTypeOfFiles");

  @Inject private UploadFileUtil uploadFileUtil;
  @Inject private SeleniumWebDriver seleniumWebDriver;
  @Inject private DefaultTestUser user;

  @BeforeClass
  public void setup() {}

  @Test
  public void shouldUploadFile() {}

  @Test
  public void shouldUploadFileWithOveriting() {}

  @Test
  public void shouldUploadFileWithoutOverwriting() {}

  @Test
  public void shouldUploadDirectory() {}

  @Test
  public void shouldUploadDirectoryWithOverwriting() {}

  @Test
  public void shouldUploadDirectoryWithoutOverwriting() {}

  @Test
  public void shouldUploadDirectoryDoNotSkippingRoot() {}

  @Test
  public void shouldUploadDirectorySkippingRoot() {}
}
