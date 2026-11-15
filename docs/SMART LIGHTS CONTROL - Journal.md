# Preliminary
- familiarized with Figma file and Requirements pdf
- wrote down first-sigth observations in [SMART LIGTHS CONTROL - Notes]
- set up new project in Android Studio
- set up agent with API key provided
- set up Figma MCP
- rename asset images and move them into assets
- import requirements in /docs for refernce
- first overall feed to agent of requirements, results to be fine tuned

# Operational
## Step 0
- Figma MCP fails for lack of edit permissions on the Figma file, i'll provide screenshots, hoping for the best...
- Clarification to agent for Step 0
	- Most of you questions will be answered by analyzing the screenshots I've provided in the raw folder (raw/homescreen_screenshot and raw/ligth_modal_open)
	- The mock up shows a signle widget for the ligth control. I've been thinkin about it : 
	  (HARD WAY) we could think of enabling the top house image as a whole system representaion of the avilable ligths -> the user taps the relative ligth they want to control and the widget enable the control over that chosen ligth. 
	  (EASIER WAY) Alternatively a simple dropdown selector on the side , and the relative ligth on the house image is highlighted 
	- "group ligth" means all the ligth in the house. Acting on the group changes all the ligths(in our case the bedligth and the other non-dimmable ligth)
	- Criterion 1 exemplifies what the label should show: the state (on/of) and the percentage (0-100%). this last indicator only applies to dimmable ligth
	- swiping on a ligth that is off works as an on/off tap: swiping rigth a ligth that is off ligth it up, and swiping left reaching 0 will ligth it off. For non dimmable works as a directional toggle
	- touch targets should follow the android guideline (the requirements cite a 44x44 pt minumum, so 48dp should be ok)
- the "group ligth" is a trickier req then it seemed.
  I'm thinking of starting with a simple on/of (0-100) and maybe think about it bettere later. (eg. a dimmable ligth at 75 is switched off from the group command -> goes to 0 -> when the group is switched back on it returns at 75)
- I've dropped the screenshot directly in the chat cause the agent cant visualize them directly from the raw folder
- openapi hit the limit, switching to free gemini in android studio

## Step 1
- app builds correctly
- committed to repo

## Step 2
- tests are ok, let's roll back here if something deosn't match when testing real usage
- build correctly
- committed

## Step 3
- little problem with the directory having a typo in "lights", refactoring the dir and re-opening the project
- maybe the "event as state " policy is overkill and create confusion. Maybe we should evaluate the classic one-shot approach with channel or shared flow? -> looks good for now, we'll see when the actual components will use it

## Step 4
- The UI is decently rigth, we'll still have to fine-tune a couple things (see TODO)
- the sweep action feels a bit off on the emulator, let's check on the physical device---> still a bit chunky, lets try and fix that rigth away
- we should ensure recomposition optimization now too ()
- error toast: should be better handled, ideally the fact that the user is trying again to swipe or tap should dismiss the toast
- the agent misconfigured navigation and status bar paddings, fixed
- The Auto-dismiss error toast decision introduceed a regression, fixed
- the icons from the status bar are dark on a dark background , we should force them in light color

## Step 5
- Agent a bit off track regarding decisions for the drawer UI, pointed out and fixed

## Step 6
- Decided to have a pill for failure flag that cycle thru 0,10,50 and 100 % failure rate

## Step 7
- in order to polish the UI i've taken screenshots of the Figma page including the specs (layout, spacing, paddings, etc) and fed them to the agent
- the chevron padding required a fix a bit hacky (no padding on the rigth for the box containing the icon -> this preserve the touch area while satisfying the padding from the border of the widget)
- here some more fine is needed since the agent cant seem to fully grasp the details from the screenshot
- went for the extra mile adjusting the bulb assets in my personal Figma in order to make the two on/off versions consistent.
- Decided to use the bulb_on asset as the icon for the demo app

## Step 8
- enriching README file, exp. with network considerations
- refined the decisions file for better tradeoffs explanation