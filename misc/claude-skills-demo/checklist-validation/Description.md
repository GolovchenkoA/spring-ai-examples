I want to try write 2 additional projects:
misc/claude-skills-demo/checklist-validation
misc/claude-skills-demo/checklist-validation-with-ui

I'll share the idea and after that we decide if it should be 2 separate projects or the 2nd one can be an extension of the 1st one.

Idea is tooken from project misc/claude-skills-demo/document-forge where we have a custom skill that can be applied.

Problem that application solves:
There are cases when people should fill  a form out (excel or word file). For example at work.
Usually people make a lot of mistakes so after reviewing they need to rework and fix mistakes.. as a result it takes a lot of time and involves another person (a reviewer).

Solution:
People open a chat powered by Claude or It's a separate UI like we have in project misc/claude-skills-demo/document-forge
People are asked some questions and provide information which later will be automatically put in a file (word or excel or any other format if it's needed)

implementation Design:
There should be an assistent (program) which is connected to Claude Desktop or built-in in a separate UI and walk the user through all required questions to get required infomration
During that process I want to have some visualization of progress like how many answers is already asked/finished and some steps might have like 3 statuses (started, in progress, finished). Status in-progress should be when people provided not all information or some information is incorrect invalid.
So to implement it I see that assistent has 2 parts: 1st is custom skill or skills which checks that all people provided all information and information is correc. 2nd part is some document templates which of course can be used by people as examples, but what most important there should be hidden templates for the assistant which can be used to get known how a correct information look like and then check if information for new documents is correct