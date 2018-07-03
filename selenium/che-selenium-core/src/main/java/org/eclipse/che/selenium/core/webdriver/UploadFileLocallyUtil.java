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

import com.google.inject.Singleton;
import java.nio.file.Path;
import org.eclipse.che.selenium.core.SeleniumWebDriver;

/**
 * This is set of methods to work with files which are uploaded by WebDriver locally.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class UploadFileLocallyUtil implements UploadFileUtil {

  @Override
  public Path prepareFileToUpload(SeleniumWebDriver seleniumWebDriver, Path localPath) {
    return localPath;
  }

  @Override
  public Path prepareDirectoryToUpload(SeleniumWebDriver seleniumWebDriver, Path localPath) {
    // TODO
    return null;
  }
}
