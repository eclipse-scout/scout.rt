/*******************************************************************************
 * Copyright (c) 2013 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.client.services.common.file;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.scout.rt.platform.BeanMetaData;
import org.eclipse.scout.rt.platform.IBean;
import org.eclipse.scout.rt.platform.exception.ProcessingException;
import org.eclipse.scout.rt.platform.util.IOUtility;
import org.eclipse.scout.rt.shared.services.common.file.IRemoteFileService;
import org.eclipse.scout.rt.shared.services.common.file.RemoteFile;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.shared.TestingUtility;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link FileService}
 */
@RunWith(PlatformTestRunner.class)
public class FileServiceTest {

  private static final String TEST_DIR_IN = "FileServiceTestIN";
  private static final String TEST_DIR_OUT = "FileServiceTestOUT";
  private static final String TEST_PATH_1 = TEST_DIR_IN + "/" + "file1.txt";
  private static final String TEST_PATH_2 = TEST_DIR_IN + "/sub/" + "file2.txt";

  @Before
  public void createTestFiles() {
    deleteTestFolders();

    try {
      File folder = new File(TEST_DIR_IN);
      folder.mkdir();
      folder.createNewFile();

      //create non-empty testfile
      File f = new File(TEST_PATH_1);
      f.createNewFile();
      FileWriter fstream = new FileWriter(f);
      BufferedWriter out = new BufferedWriter(fstream);
      out.write(".");
      out.close();

      //create non-empty testfile in a subfolder
      File subDir = new File(TEST_DIR_IN + "/sub");
      subDir.mkdirs();

      File f2 = new File(TEST_PATH_2);
      BufferedWriter out2 = new BufferedWriter(new FileWriter(f2));
      out2.write("...");
      out2.close();
      f2.createNewFile();
    }
    catch (IOException e) {
      e.printStackTrace();
      fail();
    }
  }

  @After
  public void cleanupTestFiles() {
    deleteTestFolders();
  }

  private void deleteTestFolders() {
    IOUtility.deleteDirectory(new File(TEST_DIR_IN));
    IOUtility.deleteDirectory(new File(TEST_DIR_OUT));
  }

  @Test
  public void syncRemoteFilesToPath() {
    //register services
    List<IBean<?>> reg = TestingUtility.registerBeans(
        new BeanMetaData(DummyRemoteFileService.class)
            .withInitialInstance(new DummyRemoteFileService())
            .withApplicationScoped(true));

    //start with clean setup
    File outFolder = new File(TEST_DIR_OUT);
    assertFalse("Folder is not synchronized yet, but exists", outFolder.exists());

    try {
      //synchronize test folder to path
      FileService svc = new FileService();
      svc.syncRemoteFilesToPath(TEST_DIR_OUT, TEST_DIR_IN, null);

      File synchronizedFile = new File(TEST_PATH_1);
      File synchronizedFile2 = new File(TEST_PATH_2);
      File synchronizedFolder = new File(TEST_DIR_OUT);

      assertTrue("Synchronized file does not exist " + synchronizedFile, synchronizedFile.exists());
      assertTrue("Synchronized file does not exist " + synchronizedFile2, synchronizedFile2.exists());
      assertTrue("Synchronized folder does not exist " + synchronizedFolder, synchronizedFolder.exists());
      assertTrue("Incorrect number of synchronized files", synchronizedFolder.listFiles().length == 2);

      //synchronize again
      svc.syncRemoteFilesToPath(TEST_DIR_OUT, TEST_DIR_IN, null);
      assertTrue("Synchronized file does not exist " + synchronizedFile, synchronizedFile.exists());
      assertTrue("Synchronized file does not exist " + synchronizedFile2, synchronizedFile2.exists());
      assertTrue("Synchronized folder does not exist " + synchronizedFolder, synchronizedFolder.exists());
      assertTrue("Incorrect number of synchronized files", synchronizedFolder.listFiles().length == 2);

    }
    finally {
      TestingUtility.unregisterBeans(reg);
    }
  }

  /**
   * Dummy remote file service to test fileservice
   */
  private static class DummyRemoteFileService implements IRemoteFileService {

    @Override
    public RemoteFile getRemoteFile(RemoteFile spec) {
      throw new ProcessingException("not implemented");
    }

    @Override
    public RemoteFile getRemoteFileHeader(RemoteFile spec) {
      throw new ProcessingException("not implemented");
    }

    @Override
    public RemoteFile getRemoteFilePart(RemoteFile spec, long blockNumber) {
      throw new ProcessingException("not implemented");
    }

    @Override
    public RemoteFile[] getRemoteFiles(String folderPath, FilenameFilter filter, RemoteFile[] existingFileInfoOnClient) {
      try {
        RemoteFile r1 = new RemoteFile(new URL("file:" + TEST_PATH_1), false);
        RemoteFile r2 = new RemoteFile(new URL("file:" + TEST_PATH_2), false);
        return new RemoteFile[]{r1, r2};
      }
      catch (MalformedURLException e) {
        throw new ProcessingException("", e);
      }
    }

    @Override
    public void putRemoteFile(RemoteFile spec) {
      throw new ProcessingException("not implemented");
    }

    @Override
    public void streamRemoteFile(RemoteFile spec, OutputStream out) {
      throw new ProcessingException("not implemented");
    }

  }
}
