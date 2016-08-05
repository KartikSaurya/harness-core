package software.wings.beans.command;

import com.github.reinert.jjschema.Attributes;
import com.github.reinert.jjschema.SchemaIgnore;
import software.wings.stencils.DefaultValue;

import java.util.List;

/**
 * Created by anubhaw on 7/14/16.
 */
public class ProcessCheckRunningCommandUnit extends ExecCommandUnit {
  /**
   * Instantiates a new Process check command unit.
   */
  public ProcessCheckRunningCommandUnit() {
    super();
    setCommandUnitType(CommandUnitType.PROCESS_CHECK_RUNNING);
  }

  @SchemaIgnore
  @Override
  public String getCommandPath() {
    return super.getCommandPath();
  }

  @Attributes(title = "Command")
  @DefaultValue("set -x\npgrep -f \"\\-Dcatalina.home=$WINGS_RUNTIME_PATH/tomcat\"")
  @Override
  public String getCommandString() {
    return super.getCommandString();
  }

  @SchemaIgnore
  @Override
  public List<TailFilePatternEntry> getTailPatterns() {
    return super.getTailPatterns();
  }

  @SchemaIgnore
  @Override
  public boolean isTailFiles() {
    return super.isTailFiles();
  }

  @SchemaIgnore
  @Override
  public String getPreparedCommand() {
    return super.getPreparedCommand();
  }
}
