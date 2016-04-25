/******************************************************************************
 *
 * Copyright (c) 1998,99 by Mindbright Technology AB, Stockholm, Sweden.
 *                 www.mindbright.se, info@mindbright.se
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *****************************************************************************
 * $Author: josh $
 * $Date: 2001/02/03 00:47:00 $
 * $Name:  $
 *****************************************************************************/
package es.ssh.ssh;

import es.ssh.terminal.Terminal;
import es.ssh.security.Cipher;

public interface SSHConsole {
  public Terminal getTerminal();

  public void stdoutWriteString(byte[] str);
  public void stderrWriteString(byte[] str);

  public void print(String str);
  public void println(String str);

  public void serverConnect(SSHChannelController controller, Cipher sndCipher);
  public void serverDisconnect(String reason);
}
