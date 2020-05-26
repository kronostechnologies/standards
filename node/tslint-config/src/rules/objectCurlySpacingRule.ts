/**
 * This rule is a copy of tslint-eslint-rules:
 *
 * source file: https://github.com/buzinas/tslint-eslint-rules/blob/master/src/rules/objectCurlySpacingRule.ts
 * git commit hash: 24190b7059b8c947d7df5b731434566fdd6b5468
 */

import * as Lint from 'tslint';
import * as ts from 'typescript';

const OPTION_ALWAYS = 'always';

export class Rule extends Lint.Rules.AbstractRule {
    public static FAILURE_STRING = {
        always: {
            start: `A space is required after '{'`,
            end: `A space is required before '}'`,
        },
        never: {
            start: `There should be no space after '{'`,
            end: `There should be no space before '}'`,
        },
    };

    public apply(sourceFile: ts.SourceFile): Lint.RuleFailure[] {
        const walker = new ObjectCurlySpacingWalker(sourceFile, this.getOptions());
        return this.applyWithWalker(walker);
    }
}

class ObjectCurlySpacingWalker extends Lint.RuleWalker {
    private readonly always: boolean;
    private readonly exceptions: {
        arraysInObjects: boolean;
        objectsInObjects: boolean;
    };

    constructor(sourceFile: ts.SourceFile, options: Lint.IOptions) {
        super(sourceFile, options);

        const opt = this.getOptions();
        this.always = this.hasOption(OPTION_ALWAYS) || (opt && opt.length === 0);
        this.exceptions = opt[1] || {};

        if (this.exceptions.arraysInObjects === undefined) {
            this.exceptions.arraysInObjects = this.always;
        }
        if (this.exceptions.objectsInObjects === undefined) {
            this.exceptions.objectsInObjects = this.always;
        }
    }

    protected visitNode(node: ts.Node): void {
        const bracedKind = [
            ts.SyntaxKind.ObjectLiteralExpression,
            ts.SyntaxKind.ObjectBindingPattern,
            ts.SyntaxKind.NamedImports,
            ts.SyntaxKind.NamedExports,
        ];
        if (bracedKind.indexOf(node.kind) > -1) {
            this.checkSpacingInsideBraces(node);
        }
        super.visitNode(node);
    }

    private checkSpacingInsideBraces(node: ts.Node): void {
        const text = node.getText();
        if (text.indexOf('\n') !== -1 || /^\{\s*\}$/.test(text)) {
            // Rule does not apply when the braces span multiple lines
            return;
        }

        // Lookup whether the last value in the object is an object or array literal
        let endsWithObjectLiteral = false;
        let endsWithArrayLiteral = false;
        if (node.getChildren().length === 3) {
            const contents = node.getChildren()[1].getChildren();
            if (contents.length > 0) {
                const lastElement = contents[contents.length - 1];
                if (lastElement.kind === ts.SyntaxKind.PropertyAssignment || lastElement.kind
                    === ts.SyntaxKind.BindingElement) {
                    const value = lastElement.getChildren();
                    if (value.length === 3) {
                        endsWithObjectLiteral = value[2].kind === ts.SyntaxKind.ObjectLiteralExpression || value[2].kind
                            === ts.SyntaxKind.ObjectBindingPattern;
                        endsWithArrayLiteral = value[2].kind === ts.SyntaxKind.ArrayLiteralExpression;
                    }
                }
            }
        }

        // We have matching braces, lets find out number of leading spaces
        const leadingSpace = text.match(/^\{(\s{0,2})/)![1].length;
        if (this.always) {
            if (leadingSpace === 0) {
                const fix = Lint.Replacement.appendText(node.getStart() + 1, ' ');
                this.addFailure(this.createFailure(node.getStart(), 1, Rule.FAILURE_STRING.always.start, fix));
            }
        } else {
            if (leadingSpace > 0) {
                const fix = Lint.Replacement.deleteText(node.getStart() + 1, leadingSpace);
                this.addFailure(this.createFailure(node.getStart(), 1, Rule.FAILURE_STRING.never.start, fix));
            }
        }

        // Finding trailing spaces requires checking if exceptions apply, and adjusting accordingly
        const trailingSpace = text.match(/(\s{0,2})}$/)![1].length;
        const arrayExceptionApplies = this.always !== this.exceptions.arraysInObjects && endsWithArrayLiteral;
        const objectExceptionApplies = this.always !== this.exceptions.objectsInObjects && endsWithObjectLiteral;
        const spaceRequired = arrayExceptionApplies || objectExceptionApplies ? !this.always : this.always;
        if (spaceRequired) {
            if (trailingSpace === 0) {
                const fix = Lint.Replacement.appendText(node.getEnd() - 1, ' ');
                this.addFailure(this.createFailure(node.getEnd() - 1, 1, Rule.FAILURE_STRING.always.end, fix));
            }
        } else {
            if (trailingSpace > 0) {
                const fix = Lint.Replacement.deleteText(node.getEnd() - trailingSpace - 1, trailingSpace);
                this.addFailure(this.createFailure(node.getEnd() - 1, 1, Rule.FAILURE_STRING.never.end, fix));
            }
        }
    }
}
