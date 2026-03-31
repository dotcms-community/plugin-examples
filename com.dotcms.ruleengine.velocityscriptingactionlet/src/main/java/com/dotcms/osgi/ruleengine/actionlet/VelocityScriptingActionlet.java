package com.dotmarketing.osgi.ruleengine.actionlet;

import static com.dotcms.repackage.com.google.common.base.Preconditions.checkState;

import com.dotcms.repackage.com.google.common.base.Preconditions;
import com.dotmarketing.portlets.rules.RuleComponentInstance;
import com.dotmarketing.portlets.rules.actionlet.RuleActionlet;
import com.dotmarketing.portlets.rules.model.ParameterModel;
import com.dotmarketing.portlets.rules.parameter.ParameterDefinition;
import com.dotmarketing.portlets.rules.parameter.display.TextInput;
import com.dotmarketing.portlets.rules.parameter.type.TextType;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.VelocityUtil;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.context.Context;

/**
 * This actionlet allow to execute some velocity code and return the result as a request attribute.
 *
 * @author Oswaldo Gallango
 * @version 1.0
 * @since 10/17/2016
 */
public class VelocityScriptingActionlet extends RuleActionlet<VelocityScriptingActionlet.Instance> {

  /**
   * Actionlet Parameters Id
   */
  public static final String INPUT_VELOCITY_SCRIPT_KEY = "velocity_script";
  private static final String I18N_BASE = "api.system.ruleengine.actionlet.VelocityScripting";

  public VelocityScriptingActionlet() {
    super(I18N_BASE,
        new ParameterDefinition<>(1, INPUT_VELOCITY_SCRIPT_KEY, new TextInput<>(new TextType().minLength(1))));
  }

  /**
   * Create a instance of the VelocityScriptingActionlet with the provided parameters
   */
  @Override
  public Instance instanceFrom(Map<String, ParameterModel> parameters) {
    return new Instance(parameters);
  }

  /**
   * This method evaluates the parameters passed and set the velocity code result in a request
   * attribute
   * 
   * @param request Http servlet request
   * @param response Http servlet response
   * @param instance VelocityScriptingActionlet instance
   * 
   * @return true if the code could be evaluated, false if not
   */
  @Override
  public boolean evaluate(HttpServletRequest request, HttpServletResponse response, Instance instance) {
    boolean success = false;
    try {
      Context ctx = VelocityUtil.getWebContext(request, response);
      String code = instance.velocity_script;
      code = unescapeVelocityQuotes(code);
      new VelocityUtil().parseVelocity(code, ctx);
      success = true;
    } catch (Exception e) {
      Logger.error(VelocityScriptingActionlet.class, "Error executing Velocity Scripting Actionlet.", e);
    }
    return success;
  }

  /**
   * Unescape velocity quotes and single quotes
   * 
   * @param code String with the velocity script to unescape
   * 
   * @return String with quotes unescaped
   */
  private String unescapeVelocityQuotes(String code) {
    code = code.replaceAll("\\$\\{quote}", "\"");
    code = code.replaceAll("\\$quote ", "\"");

    code = code.replaceAll("\\$\\{singleQuote}", "\'");
    code = code.replaceAll("\\$singleQuote ", "\'");

    return code;
  }

  public class Instance implements RuleComponentInstance {

    private final String velocity_script;

    public Instance(Map<String, ParameterModel> parameters) {
      checkState(parameters != null && parameters.size() == 1,
          "Velocity Scripting Condition Type requires parameter '%s'.",  INPUT_VELOCITY_SCRIPT_KEY);
      assert parameters != null;
      this.velocity_script = parameters.get(INPUT_VELOCITY_SCRIPT_KEY).getValue();
      Preconditions.checkArgument(StringUtils.isNotBlank(this.velocity_script),
          "VelocityScriptingActionlet requires valid velocity code.");

    }
  }
}
