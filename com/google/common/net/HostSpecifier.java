package com.google.common.net;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import java.net.InetAddress;
import java.text.ParseException;
import javax.annotation.Nullable;

@Beta
public final class HostSpecifier {
  private final String canonicalForm;
  
  private HostSpecifier(String canonicalForm) {
    this.canonicalForm = canonicalForm;
  }
  
  public static HostSpecifier fromValid(String specifier) {
    HostAndPort parsedHost = HostAndPort.fromString(specifier);
    Preconditions.checkArgument(!parsedHost.hasPort());
    String host = parsedHost.getHostText();
    InetAddress addr = null;
    try {
      addr = InetAddresses.forString(host);
    } catch (IllegalArgumentException e) {}
    if (addr != null)
      return new HostSpecifier(InetAddresses.toUriString(addr)); 
    InternetDomainName domain = InternetDomainName.from(host);
    if (domain.hasPublicSuffix())
      return new HostSpecifier(domain.toString()); 
    String.valueOf(host);
    throw new IllegalArgumentException((String.valueOf(host).length() != 0) ? "Domain name does not have a recognized public suffix: ".concat(String.valueOf(host)) : new String("Domain name does not have a recognized public suffix: "));
  }
  
  public static HostSpecifier from(String specifier) throws ParseException {
    try {
      return fromValid(specifier);
    } catch (IllegalArgumentException e) {
      String.valueOf(specifier);
      ParseException parseException = new ParseException((String.valueOf(specifier).length() != 0) ? "Invalid host specifier: ".concat(String.valueOf(specifier)) : new String("Invalid host specifier: "), 0);
      parseException.initCause(e);
      throw parseException;
    } 
  }
  
  public static boolean isValid(String specifier) {
    try {
      fromValid(specifier);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    } 
  }
  
  public boolean equals(@Nullable Object other) {
    if (this == other)
      return true; 
    if (other instanceof HostSpecifier) {
      HostSpecifier that = (HostSpecifier)other;
      return this.canonicalForm.equals(that.canonicalForm);
    } 
    return false;
  }
  
  public int hashCode() {
    return this.canonicalForm.hashCode();
  }
  
  public String toString() {
    return this.canonicalForm;
  }
}
