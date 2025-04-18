/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

grammar Directives;

options {
  language = Java;
}

@lexer::header {
/*
 * Copyright © 2017-2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
}

/**
 * Parser Grammar for recognizing tokens and constructs of the directives language.
 */
recipe
 : statements EOF
 ;

statements
 :  ( Comment | macro | directive SColon | pragma SColon | ifStatement)*
 ;

directive
 : command
  (   codeblock
    | identifier
    | macro
    | text
    | number
    | bool
    | column
    | colList
    | numberList
    | boolList
    | stringList
    | numberRanges
    | properties
    | byteSizeArg
    | timeDurationArg
  )*?
  ;

ifStatement
  : ifStat elseIfStat* elseStat? '}'
  ;

ifStat
  : If expression OBrace statements
  ;

elseIfStat
  : CBrace Else If expression OBrace statements
  ;

elseStat
  : CBrace Else OBrace statements
  ;

expression
  : '(' (~'(' | expression)* ')'
  ;

forStatement
 : For OParen Identifier Assign expression SColon expression SColon expression CParen OBrace statements CBrace
 ;

macro
 : Dollar OBrace (~OBrace | macro | Macro)*? CBrace
 ;

pragma
 : Pragma (pragmaLoadDirective | pragmaVersion)
 ;

pragmaLoadDirective
 : LoadDir identifierList
 ;

pragmaVersion
 : Version Number
 ;

codeblock
 : Exp Space* Colon condition
 ;

identifier
 : Identifier
 ;

properties
 : Prop Colon OBrace (propertyList)+  CBrace
 | Prop Colon OBrace OBrace (propertyList)+ CBrace { notifyErrorListeners("Too many start paranthesis"); }
 | Prop Colon OBrace (propertyList)+ CBrace CBrace { notifyErrorListeners("Too many start paranthesis"); }
 | Prop Colon (propertyList)+ CBrace { notifyErrorListeners("Missing opening brace"); }
 | Prop Colon OBrace (propertyList)+  { notifyErrorListeners("Missing closing brace"); }
 ;

propertyList
 : property (Comma property)*
 ;

property
 : Identifier Assign ( text | number | bool )
 ;

numberRanges
 : numberRange ( Comma numberRange)*
 ;

numberRange
 : Number Colon Number Assign value
 ;

value
 : String | Number | Column | Bool | byteSizeArg | timeDurationArg
 ;

ecommand
 : External Identifier
 ;

config
 : Identifier
 ;

column
 : Column
 ;

text
 : String
 ;

number
 : Number
 ;

bool
 : Bool
 ;

condition
 : OBrace (~CBrace | condition)* CBrace
 ;

command
 : Identifier
 ;

colList
 : Column (Comma Column)+
 ;

numberList
 : Number (Comma Number)+
 ;

boolList
 : Bool (Comma Bool)+
 ;

stringList
 : String (Comma String)+
 ;

identifierList
 : Identifier (Comma Identifier)*
 ;

// Add new parser rules for byte size and time duration arguments
byteSizeArg
 : ByteSize
 ;

timeDurationArg
 : TimeDuration
 ;

/*
 * Following are the Lexer Rules used for tokenizing the recipe.
 */
OBrace   : '{';
CBrace   : '}';
SColon   : ';';
Or       : '||';
And      : '&&';
Equals   : '==';
NEquals  : '!=';
GTEquals : '>=';
LTEquals : '<=';
Match    : '=~';
NotMatch : '!~';
QuestionColon : '?:';
StartsWith : '=^';
NotStartsWith : '!^';
EndsWith : '=$';
NotEndsWith : '!$';
PlusEqual : '+=';
SubEqual : '-=';
MulEqual : '*=';
DivEqual : '/=';
PerEqual : '%=';
AndEqual : '&=';
OrEqual  : '|=';
XOREqual : '^=';
Pow      : '^';
External : '!';
GT       : '>';
LT       : '<';
Add      : '+';
Subtract : '-';
Multiply : '*';
Divide   : '/';
Modulus  : '%';
OBracket : '[';
CBracket : ']';
OParen   : '(';
CParen   : ')';
Assign   : '=';
Comma    : ',';
QMark    : '?';
Colon    : ':';
Dot      : '.';
At       : '@';
Pipe     : '|';
BackSlash: '\\';
Dollar   : '$';
Tilde    : '~';

If        : 'if';
Else      : 'else';
For       : 'for';
Prop      : 'prop';
Exp       : 'exp';
Version   : 'version';
LoadDir   : 'load-directives';
Pragma    : '#pragma';

Bool
 : 'true'
 | 'false'
 ;

// Add these fragment definitions before the Number rule

fragment
Int 
  : '0' | [1-9] [0-9]*
  ;

fragment
Digit
  : [0-9]
  ;

Number
 : Int ('.' Digit*)?
 ;

Identifier
 : [a-zA-Z_\-] [a-zA-Z_0-9\-]*
 ;

Macro
 : [a-zA-Z_] [a-zA-Z_0-9]*
 ;

Column
 : ':' [a-zA-Z_\-] [:a-zA-Z_0-9\-]*
 ;

String
 : '\'' ( EscapeSequence | ~('\'') )* '\''
 | '"'  ( EscapeSequence | ~('"') )* '"'
 ;

EscapeSequence
   :   '\\' ('b'|'t'|'n'|'f'|'r'|'"'|'\''|'\\')
   |   UnicodeEscape
   |   OctalEscape
   ;

fragment
OctalEscape
   :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
   |   '\\' ('0'..'7') ('0'..'7')
   |   '\\' ('0'..'7')
   ;

fragment
UnicodeEscape
   :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
   ;

fragment
   HexDigit : ('0'..'9'|'a'..'f'|'A'..'F') ;

// Enhanced ByteSize lexer rule with decimal support
ByteSize
   : (Int | Float) ByteUnit
   ;

// Enhanced TimeDuration lexer rule with decimal support
TimeDuration
   : (Int | Float) TimeUnit
   ;

// Add Float fragment for decimal number support
fragment
Float
   : Int '.' Digit+
   ;

fragment
ByteUnit
   : [Kk][Bb]            // Kilobyte
   | [Mm][Bb]            // Megabyte
   | [Gg][Bb]            // Gigabyte
   | [Tt][Bb]            // Terabyte
   | [Pp][Bb]            // Petabyte
   | [Bb]                // Bytes
   | [Kk]                // Kilobytes (shorthand)
   | [Mm]                // Megabytes (shorthand)
   | [Gg]                // Gigabytes (shorthand)
   | [Tt]                // Terabytes (shorthand)
   | [Pp]                // Petabytes (shorthand)
   ;

fragment
TimeUnit
   : [Mm][Ss]            // Milliseconds
   | [Ss]                // Seconds
   | [Mm]                // Minutes
   | [Hh]                // Hours
   | [Dd]                // Days
   | [Nn][Ss]            // Nanoseconds
   | [Uu][Ss]            // Microseconds
   | [Ww]                // Weeks
   | [Mm][Oo]            // Months
   | [Yy]                // Years
   ;

Comment
 : ('//' ~[\r\n]* | '/*' .*? '*/' | '--' ~[\r\n]* ) -> skip
 ;

Space
 : [ \t\r\n\u000C]+ -> skip
 ;
