package org.springframework.jmx;

import org.springframework.jmx.metadata.support.annotations.ManagedAttribute;
import org.springframework.jmx.metadata.support.annotations.ManagedOperation;
import org.springframework.jmx.metadata.support.annotations.ManagedResource;

/**
 * @author robh
 */
@ManagedResource(objectName="bean:name=testBean4", description="My Managed Bean", log=true,
        logFile="jmx.log", currencyTimeLimit=15, persistPolicy="OnUpdate", persistPeriod=200,
        persistLocation="./foo", persistName="bar.jmx")
public class AnnotationTestBean implements IJmxTestBean {

   private String name;

    private String nickName;

    private int age;

    private boolean isSuperman;


    @ManagedAttribute(description="The Age Attribute", currencyTimeLimit=15)
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }


    @ManagedOperation(currencyTimeLimit=30)
    public long myOperation() {
        return 1L;
    }


    @ManagedAttribute(description="The Name Attribute",
            currencyTimeLimit=20,
            defaultValue="bar",
            persistPolicy="OnUpdate")
    public void setName(String name) {
        this.name = name;
    }

    @ManagedAttribute(defaultValue="foo", persistPeriod=300)
    public String getName() {
        return name;
    }


    @ManagedAttribute(description="The Nick Name Attribute")
    public String getNickName() {
        return this.nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    @ManagedAttribute(description="The Is Superman Attribute")
    public void setSuperman(boolean superman) {
        this.isSuperman = superman;
    }

    public boolean isSuperman() {
        return isSuperman;
    }

    @ManagedOperation(description="Add Two Numbers Together")
    public int add(int x, int y) {
        return x + y;
    }

    /**
     * Test method that is not exposed by the MetadataAssembler
     *
     */
    public void dontExposeMe() {
        throw new RuntimeException();
    }
}
