
package com.quantatw.sls.alljoyn;

import java.util.HashMap;

import org.alljoyn.bus.BusException;
import org.alljoyn.bus.annotation.BusInterface;
import org.alljoyn.bus.annotation.BusMethod;
import org.alljoyn.bus.annotation.BusProperty;

/*
 * The BusInterface annotation is used to tell the code this interface is an AllJoyn
 * interface.
 *
 * The 'name' value is used to specify by which name this interface will be known.  If the name is
 * not given the fully qualified name of the Java interface is be used.  In most instances its best
 * to assign an interface name since it helps promote code reuse.
 */
@BusInterface(name = "org.alljoyn.Config")
public interface ConfigCtrlInterface {

    /*
     * The BusMethod annotation signifies this function should be used as part of the AllJoyn
     * interface. The runtime is smart enough to figure out what the input and output of the
     * method is based on the input/output arguments of the method.
     *
     * All methods that use the BusMethod annotation can throw a BusException and should indicate
     * this fact.
     */
    @BusMethod (name = "FactoryReset")
    void factoryReset() throws BusException;
    @BusMethod (name = "Restart")
    void restart() throws BusException;
    @BusMethod (name = "SetPasscode", signature = "say")
    void setPasscode(String daemonRealm, byte[] newPasscode) throws BusException;
    @BusMethod (name = "GetConfigurations", signature = "s", replySignature = "a{sv}")
    HashMap<String, Object> getConfigurations(String languageTag) throws BusException;
    @BusMethod (name = "UpdateConfigurations", signature = "sa{sv}")
    void updateConfigurations(String languageTag, HashMap<String, Object> configMap) throws BusException;
    @BusMethod (name = "ResetConfigurations", signature = "sas")
    void resetConfigurations(String languageTag, String[] fieldList) throws BusException;

    /*
     * The BusProperty annotation signifies this property should be used as part of the
     * AllJoyn interface. The runtime is smart enough to figure out what the input and output of
     * the property is based on the input/output arguments of the property.
     *
     * All properties that use the BusProperty annotation can throw a BusException and should
     * indicate this fact.
     */
    @BusProperty (name = "Version", signature = "q")
    short getVersion() throws BusException;
}
