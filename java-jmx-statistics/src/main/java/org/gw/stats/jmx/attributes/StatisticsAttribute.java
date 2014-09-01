/**
 * StatisticsAttribute.java (c) Copyright 2013 AUSBOS.com
 */
package org.gw.stats.jmx.attributes;

import org.gw.stats.Statistic;
import org.gw.commons.utils.GenericsUtil;

import javax.management.MBeanAttributeInfo;

/**
 * @author gman
 * @version 1.0
 * @since 1.0
 */
public abstract class StatisticsAttribute<R, T extends Statistic> {

    protected String nameAddendum;

    protected String descAddendum;

    protected Class<R> retType;

    @SuppressWarnings("unchecked")
    public StatisticsAttribute() {
        retType = (Class<R>) GenericsUtil.getGenericTypes(getClass(), 2)[0];
    }

    public MBeanAttributeInfo getMBeanAttributeInfo(T statistic) {
        return new MBeanAttributeInfo(statistic.getName() + getNameAddendum(statistic),
                retType.getCanonicalName(), statistic.getName() + getNameAddendum(statistic), true,
                true, false);
    }

    public String getStatisticName(String attribute) {
        String statName = attribute;
        if (attribute.contains(":")) {
            statName = attribute.substring(0, attribute.indexOf(":"));
        }
        return statName;
    }

    public abstract R getValue(T statistic);

    public boolean show(T statistic) {
        return statistic.getEnabled().get();
    }

    public Class<R> getRetType() {
        return retType;
    }

    public String getNameAddendum(Statistic stat) {
        return nameAddendum;
    }

    public String getDescAddendum(Statistic stat) {
        return descAddendum;
    }
}
