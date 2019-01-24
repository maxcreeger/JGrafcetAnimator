package com.beetlekhi.animator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.beetlekhi.grafcet.model.Executed;
import com.beetlekhi.grafcet.model.Expression;
import com.beetlekhi.grafcet.model.Grafcet;
import com.beetlekhi.grafcet.model.GrafcetUtils;
import com.beetlekhi.grafcet.model.Required;
import com.beetlekhi.grafcet.model.Step;
import com.beetlekhi.grafcet.model.Transition;
import com.beetlekhi.grafcet.model.Variable;

public class GrafcetAnimator {

    private final Grafcet grafcet;
    private final Map<Integer, Step> active = new HashMap<>();
    private final Map<String, AtomicBoolean> booleanVariables = new HashMap<>();
    private final Map<String, AtomicInteger> integerVariables = new HashMap<>();

    public GrafcetAnimator(Grafcet grafcet) {
        this.grafcet = grafcet;

        // Register initial steps
        for (Step step : grafcet.getSteps().getStep()) {
            if (step.isInitial()) {
                active.put(step.getNum(), step);
            }
        }

        // Register variables
        for (Variable variable : grafcet.getVariables().getVariable()) {
            switch (variable.getType()) {
            case "integer":
                AtomicInteger intValue = variable.getInitial() != null ? new AtomicInteger(Integer.parseInt(variable.getInitial())) : new AtomicInteger();
                integerVariables.put(variable.getName(), intValue);
                break;
            case "boolean":
                AtomicBoolean boolValue = variable.getInitial() != null ? new AtomicBoolean(Boolean.parseBoolean(variable.getInitial())) : new AtomicBoolean();
                booleanVariables.put(variable.getName(), boolValue);
                break;
            default:
                throw new UnsupportedOperationException("Don't know type: " + variable.getType() + " for variable named '" + variable.getName() + "'");
            }
        }
    }

    public boolean isActive(Integer stepNum) {
        return active.containsKey(stepNum);
    }

    public boolean isEnabled(Integer transitionNum) {
        return isTransitionEnabled(GrafcetUtils.getTransition(grafcet, transitionNum));
    }

    public void animate() {
        List<Transition> transitions = transitionsEnabled();
        transitions.removeIf(tran -> !isValid(tran));
        transitions.forEach(this::deactivateRequiredSteps);
        transitions.forEach(this::activateExecutedSteps);
    }

    public Integer getIntegerVariable(String name) {
        AtomicInteger value = integerVariables.get(name);
        if (value == null) {
            return null;
        } else {
            return value.get();
        }
    }

    public Boolean getBooleanVariable(String name) {
        AtomicBoolean value = booleanVariables.get(name);
        if (value == null) {
            return null;
        } else {
            return value.get();
        }
    }

    private void deactivateRequiredSteps(Transition tran) {
        if (tran.getRequiredSteps() != null) {
            for (Required deactivated : tran.getRequiredSteps().getRequired()) {
                active.remove(deactivated.getStep());
            }
        }
    }

    private void activateExecutedSteps(Transition tran) {
        if (tran.getExecutedSteps() != null) {
            for (Executed activated : tran.getExecutedSteps().getExecuted()) {
                active.put(activated.getStep(), GrafcetUtils.getStep(grafcet, activated.getStep()));
            }
        }
    }

    private boolean isTransitionEnabled(Transition transition) {
        List<Step> required = GrafcetUtils.getRequiredSteps(grafcet, transition.getNum());
        return required.stream().allMatch(step -> active.containsKey(step.getNum()));
    }

    private List<Transition> transitionsEnabled() {
        return grafcet.getTransitions().getTransition().stream().filter(this::isTransitionEnabled).collect(Collectors.toList());
    }

    private boolean isValid(Transition transition) {
        Expression condition = transition.getCondition();
        return condition == null || isValid(condition);
    }

    private boolean isValid(Expression expr) {
        switch (expr.getOp()) {
        case "true":
            return true;
        case "false":
            return false;
        case "step":
            String value = expr.getValue();
            Integer stepNum = Integer.parseInt(value);
            return isActive(stepNum);
        case "variable":
            String variableName = expr.getValue();
            AtomicBoolean boolValue = booleanVariables.get(variableName);
            return boolValue.get();
        case "band":
            return isValid(expr.getLeft()) && isValid(expr.getRight());
        case "bor":
            return isValid(expr.getLeft()) || isValid(expr.getRight());
        default:
            throw new UnsupportedOperationException("wtf is " + expr);
        }

    }

}
