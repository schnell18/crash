import javax.jcr.Node;
import javax.jcr.Session;
import javax.jcr.Workspace;

import org.crsh.command.ScriptException;
import org.kohsuke.args4j.Argument;
import org.crsh.command.Description;
import org.crsh.command.CommandContext;

@Description("Move a node to another location")
public class mv extends org.crsh.command.BaseCommand<Node, Void> {

  @Argument(required=false,index=0,usage="The path of the source node to move")
  def String source;

  @Argument(required=false,index=1,usage="The destination path")
  def String target;

  public void execute(CommandContext<Node, Void> context) throws ScriptException {
    assertConnected()

    //
    if (context.piped) {
      if (target != null)
        throw new ScriptException("Only one argument is permitted when involved in a pipe");
      def targetParent = findNodeByPath(source);
      context.consume().each { node ->
        def targetPath = targetParent.path + "/" + node.name;
        session.workspace.move(node.path, targetPath);
      };
    } else {
      def sourceNode = findNodeByPath(source);
      def targetPath = absolutePath(target);
      sourceNode.session.workspace.move(sourceNode.path, targetPath);
    }
  }
}