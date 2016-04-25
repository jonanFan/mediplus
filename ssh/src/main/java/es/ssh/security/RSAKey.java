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
package es.ssh.security;

import java.math.BigInteger;

public class RSAKey implements Key {

  private BigInteger e;
  private BigInteger n;

  protected RSAKey(BigInteger e, BigInteger n) {
    this.e    = e;
    this.n    = n;
  }

  public String getAlgorithm() {
    return "RSA";
  }

  public byte[] getEncoded() {
    return null;
  }

  public String getFormat() {
    return null;
  }

  public int bitLength() {
    return n.bitLength();
  }

  public BigInteger getE() {
    return e;
  }

  public BigInteger getN() {
    return n;
  }

}
