package com.codicesoftware.plugins.hudson;

import com.codicesoftware.plugins.hudson.commands.CleanupWorkspaceCommand;
import com.codicesoftware.plugins.hudson.commands.CommandRunner;
import com.codicesoftware.plugins.hudson.commands.DeleteWorkspaceCommand;
import com.codicesoftware.plugins.hudson.commands.GetWorkspaceFromPathCommand;
import com.codicesoftware.plugins.hudson.commands.ListWorkspacesCommand;
import com.codicesoftware.plugins.hudson.commands.NewWorkspaceCommand;
import com.codicesoftware.plugins.hudson.commands.SetSelectorCommand;
import com.codicesoftware.plugins.hudson.commands.UndoCheckoutCommand;
import com.codicesoftware.plugins.hudson.model.CleanupMethod;
import com.codicesoftware.plugins.hudson.model.Workspace;
import hudson.FilePath;
import hudson.remoting.VirtualChannel;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.UUID;

public class WorkspaceManager {

    private WorkspaceManager() { }

    public static List<Workspace> loadWorkspaces(PlasticTool tool, VirtualChannel channel)
            throws IOException, InterruptedException, ParseException {
        ListWorkspacesCommand command = new ListWorkspacesCommand(channel);
        return CommandRunner.executeAndRead(tool, command);
    }

    public static Workspace createWorkspace(
            PlasticTool tool, FilePath workspacePath, String workspaceName, String selector)
            throws IOException, InterruptedException, ParseException {
        FilePath selectorPath = workspacePath.createTextTempFile("selector", ".txt", selector);
        NewWorkspaceCommand mkwkCommand = new NewWorkspaceCommand(workspaceName, workspacePath, selectorPath);
        CommandRunner.execute(tool, mkwkCommand);
        selectorPath.delete();
        GetWorkspaceFromPathCommand gwpCommand = new GetWorkspaceFromPathCommand(workspacePath);
        return CommandRunner.executeAndRead(tool, gwpCommand);
    }

    public static void deleteWorkspace(PlasticTool tool, FilePath workspacePath)
            throws IOException, InterruptedException {
        DeleteWorkspaceCommand command = new DeleteWorkspaceCommand(workspacePath.getRemote());
        CommandRunner.execute(tool, command);
    }

    public static void cleanWorkspace(PlasticTool tool, FilePath workspacePath, CleanupMethod cleanup)
            throws IOException, InterruptedException {
        if (cleanup.removesPrivate()) {
            CleanupWorkspaceCommand cleanupCommands = new CleanupWorkspaceCommand(
                workspacePath.getRemote(), cleanup.removesIgnored());
            CommandRunner.execute(tool, cleanupCommands);
        }
        UndoCheckoutCommand command = new UndoCheckoutCommand(workspacePath.getRemote());
        CommandRunner.execute(tool, command);
    }

    public static void setSelector(PlasticTool tool, FilePath workspacePath, String selector)
            throws IOException, InterruptedException {
        FilePath selectorPath = workspacePath.createTextTempFile("selector", ".txt", selector);
        SetSelectorCommand command = new SetSelectorCommand(workspacePath.getRemote(), selectorPath.getRemote());
        CommandRunner.execute(tool, command);
        selectorPath.delete();
    }

    public static String generateUniqueWorkspaceName() {
        return "jenkins_" + UUID.randomUUID().toString().replaceAll("-", "");
    }
}
