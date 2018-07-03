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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.nio.file.Path;
import javax.inject.Named;
import org.eclipse.che.selenium.core.SeleniumWebDriver;

/**
 * This is set of methods to work with files which are uploaded by WebDriver inside selenium node.
 *
 * @author Dmytro Nochevnov
 */
@Singleton
public class UploadFileIntoGridUtil implements UploadFileUtil {

  private final String uploadDir;

  @Inject
  public UploadFileIntoGridUtil(@Named("tests.tmp_dir") String uploadDir) {
    this.uploadDir = uploadDir;
  }

  @Override
  public Path prepareFileToUpload(SeleniumWebDriver seleniumWebDriver, Path localPath)
      throws IOException {
    // TODO
    return null;
  }

  @Override
  public Path prepareDirectoryToUpload(SeleniumWebDriver seleniumWebDriver, Path localPath)
      throws IOException {
    // TODO
    return null;
  }
}
