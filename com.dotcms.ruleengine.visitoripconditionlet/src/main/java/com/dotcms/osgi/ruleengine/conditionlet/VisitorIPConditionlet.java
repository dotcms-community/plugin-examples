package com.dotmarketing.osgi.ruleengine.conditionlet;

import static com.dotcms.repackage.com.google.common.base.Preconditions.checkState;
import static com.dotmarketing.portlets.rules.parameter.comparison.Comparison.IS;
import static com.dotmarketing.portlets.rules.parameter.comparison.Comparison.IS_NOT;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.dotcms.repackage.org.apache.commons.net.util.SubnetUtils;
import com.dotcms.util.HttpRequestDataUtil;
import com.dotmarketing.portlets.rules.RuleComponentInstance;
import com.dotmarketing.portlets.rules.conditionlet.ComparisonParameterDefinition;
import com.dotmarketing.portlets.rules.conditionlet.Conditionlet;
import com.dotmarketing.portlets.rules.exception.ComparisonNotPresentException;
import com.dotmarketing.portlets.rules.exception.ComparisonNotSupportedException;
import com.dotmarketing.portlets.rules.model.ParameterModel;
import com.dotmarketing.portlets.rules.parameter.ParameterDefinition;
import com.dotmarketing.portlets.rules.parameter.comparison.Comparison;
import com.dotmarketing.portlets.rules.parameter.display.TextInput;
import com.dotmarketing.portlets.rules.parameter.type.TextType;
import com.dotmarketing.util.Logger;

/**
 * This conditionlet will allow CMS users to check if the visitor IP is inside a CIDR netmask
 *
 * @author Oswaldo Gallango
 * @version 1.0
 * @since 10/17/2016
 */
public class VisitorIPConditionlet extends Conditionlet<VisitorIPConditionlet.Instance> {

  private static final long serialVersionUID = 1L;

  public static final String CIDR_NETMASK_RANGE__KEY = "cidr_netmask";
  private static final String I18N_BASE = "api.ruleengine.system.conditionlet.VisitorIPCIDR";

  private static final ParameterDefinition<TextType> netmask =
      new ParameterDefinition<>(2, CIDR_NETMASK_RANGE__KEY, new TextInput<>(new TextType().required()));


  public VisitorIPConditionlet() {
    super(I18N_BASE, new ComparisonParameterDefinition(1, IS, IS_NOT), netmask);
  }

  /**
   * This method evaluates if the conditionlet conditions are met
   * 
   * @param request Http servlet request
   * @param response Http servlet response
   * @param instance VelocityScriptingActionlet VisitorIPConditionlet
   * 
   * @return true if the conditions are met, false if not
   */
  @Override
  public boolean evaluate(HttpServletRequest request, HttpServletResponse response, Instance instance) {
    String ipAddress = null;

    try {
      InetAddress address = HttpRequestDataUtil.getIpAddress(request);
      ipAddress = address.getHostAddress();
    } catch (UnknownHostException e) {
      Logger.error(this, "Could not retrieved a valid IP address from request: " + request.getRequestURL());
    }

    boolean compare = false;
    try {

      String ipOrNetMask = instance.netmask;
      Comparison comparison = instance.comparison;

      if (ipOrNetMask.contains("/")) {
        compare = new SubnetUtils(instance.netmask).getInfo().isInRange(ipAddress);
      } else {
        compare = ipAddress.equals(ipOrNetMask);
      }

      // if we are a "IS NOT", invert
      
      compare= (comparison == Comparison.IS) ? compare : !compare;



    } catch (Exception e) {
      Logger.error(this.getClass(), e.getMessage(), e);
    }
    return compare;
  }

  /**
   * Create a instance of the VisitorIPConditionlet with the provided parameters
   */
  @Override
  public Instance instanceFrom(Map<String, ParameterModel> parameters) {
    return new Instance(this, parameters);
  }

  public static class Instance implements RuleComponentInstance {

    public final String netmask;
    public final Comparison<String> comparison;

    public Instance(Conditionlet definition, Map<String, ParameterModel> parameters) {
      checkState(parameters != null && parameters.size() == 2,
          "Current Visitor IP Condition requires parameters %s and %s.", COMPARISON_KEY, CIDR_NETMASK_RANGE__KEY);
      assert parameters != null;
      this.netmask = parameters.get(CIDR_NETMASK_RANGE__KEY).getValue();
      String comparisonValue = parameters.get(COMPARISON_KEY).getValue();
      try {
        // noinspection unchecked
        this.comparison = ((ComparisonParameterDefinition) definition.getParameterDefinitions().get(COMPARISON_KEY))
            .comparisonFrom(comparisonValue);
      } catch (ComparisonNotPresentException e) {
        throw new ComparisonNotSupportedException("The comparison '%s' is not supported on Condition type '%s'",
            comparisonValue, definition.getId());
      }
    }
  }

}
