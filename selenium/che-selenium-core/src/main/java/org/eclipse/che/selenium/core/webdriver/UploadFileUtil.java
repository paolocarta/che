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
package org.eclipse.che.selenium.core.webdriver;

import java.io.IOException;
import java.nio.file.Path;
import org.eclipse.che.selenium.core.SeleniumWebDriver;

/**
 * This is set of methods to work with files which are uploaded by WebDriver.
 *
 * @author Dmytro Nochevnov
 */
public interface UploadFileUtil {

  /**
   * Prepare local file to be uploaded by WebDriver.
   *
   * @param seleniumWebDriver
   * @param localPath path to local file which should be uploaded by WebDriver
   * @return path to resource which WebDriver can upload
   */
  Path prepareFileToUpload(SeleniumWebDriver seleniumWebDriver, Path localPath) throws IOException;

  /**
   * Prepare local directory to be uploaded as zipped file by WebDriver.
   *
   * @param seleniumWebDriver
   * @param localPath path to local directory which should zipped and then be uploaded by WebDriver
   * @return path to resource which WebDriver can upload
   */
  Path prepareDirectoryToUpload(SeleniumWebDriver seleniumWebDriver, Path localPath)
      throws IOException;
}
