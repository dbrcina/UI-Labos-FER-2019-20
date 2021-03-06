package ui.command;

import ui.ResolutionUtils;
import ui.model.CNFClause;
import ui.model.PLModel;

public class QueryCommand extends Command {

    private final boolean verbose;

    public QueryCommand(PLModel model, boolean testing, boolean verbose) {
        super(model, testing);
        this.verbose = verbose;
    }

    @Override
    public void actionPerformed(CNFClause clause) {
        model.setGoalClause(clause);
        StringBuilder sb = new StringBuilder();
        boolean result = ResolutionUtils.plResolution(model, sb);
        if (!result || !verbose) sb.setLength(0);
        sb.append(model.getGoalClause()).append(" is ").append(result ? "true" : "unknown");
        System.out.println(sb.toString());
    }

}
