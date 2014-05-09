
// URL to the Ajax CGI script:
var AjaxScript = "cgi-bin/ajaxwrapper.py";

// List of the JSON files that contain example worlds:
var ExampleNames = ["small","medium", "large", "monster", "insanely uber large monster world"];
var ExamplesFolder = "examples";

// What the system says when it has nothing to do:
var SystemPromptText = "What can I do for you today?";

// Constants that you can play around with:
var DialogueHistory = 100;    // max nr. utterances
var FloorThickness = 10;     // pixels
var WallSeparation = 4;     // pixels
var ArmSize = 0.2;         // of stack width
var AnimationPause = 0.0; // seconds
var PromptPause = 0.0;   // seconds
var AjaxTimeout = 500;    // seconds
var ArmSpeed = 1000;   // pixels per second

// This only has effect in the latest versions of Chrome and Safari,
// the only browsers that have implemented the W3C Web Speech API:
var UseSpeech = false;

// There is no way of setting male/female voice,
// so this is one way of having different voices for user/system:
var Voices = {"system": {"lang": "en-GB", "rate": 1.1}, // British English, slightly faster
              "user": {"lang": "en-US"},  // American English
             };


//==============================================================================
//
// Don't change anything below this line, if you don't know what you are doing.
//
//==============================================================================

var CanvasWidth;
var CanvasHeight;

var Pick1 = 'pick1';
var Pick2 = 'pick2';
var Drop1 = 'drop1';
var Drop2 = 'drop2';
var Move1 = 'move1';
var Move2 = 'move2';

var SvgNS = 'http://www.w3.org/2000/svg';

var ObjectData = {
    "brick":   {"small": {"width":0.30, "height":0.30},
                "large": {"width":0.70, "height":0.60},
               },
    "plank":   {"small": {"width":0.60, "height":0.10},
                "large": {"width":1.00, "height":0.15},
               },
    "ball":    {"small": {"width":0.30, "height":0.30},
                "large": {"width":0.70, "height":0.70},
               },
    "pyramid": {"small": {"width":0.60, "height":0.25},
                "large": {"width":1.00, "height":0.40},
               },
    "box":     {"small": {"width":0.60, "height":0.30, "thickness": 0.10},
                "large": {"width":1.00, "height":0.40, "thickness": 0.10},
               },
    "table":   {"small": {"width":0.60, "height":0.30, "thickness": 0.10},
                "large": {"width":1.00, "height":0.40, "thickness": 0.10},
               },
};

var ExampleWorlds;
var currentExample;
var currentWorld;
var currentPlan;
var currentArmPosition = [];
var currentQuestion;
var lastUtterance;
function stackWidth() {
    return CanvasWidth / currentWorld.world.length;
}

function boxSpacing() {
    return Math.min(5, stackWidth() / 20);
}

$(function() {
    $('#inputform').submit(function(){
        userInput();
        return false;
    });
    $('#inputexamples').change(function(){
        userInput();
        return false;
    });
    $('#showdebug').click(function(){
        $('#debug').toggle($('#showdebug').prop('checked'));
    });
    CanvasWidth = $("#svgdiv").width() - 2 * WallSeparation;
    CanvasHeight = $("#svgdiv").height() - FloorThickness;
    resetCurrentExample(ExampleNames[0]);
});

function loadExampleWorlds() {
    ExampleWorlds = {};
    $("#exampleworlds").empty();
    $.each(ExampleNames, function(i, name) {
        $('<input type="submit">').val(name)
            .click(changeCurrentExample)
            .appendTo($("#exampleworlds"));
        $.ajax({
            dataType: "json",
            url: ExamplesFolder + "/" + name + ".json",
            async: false
        }).fail(function(jqxhr, status, error) {
            alertError("Couldn't load example '" + name + "'.json: " + status, error);
        }).done(function(world) {
            ExampleWorlds[name] = world;
        });
    });
}

function changeCurrentExample() {
    var name = $(this).val();
	if(name == "insanely uber large monster world") {
		if (confirm('Are you certain that you want to reset to "' + name + '"?')) {
			if (confirm('Are you really certain that you want to reset to "' + name + '"?')) {
				if (confirm('Are you really reaaaally certain that you want to reset to "' + name + '"?')) {
					if (confirm('Are you REAAAAAAALLY REAAAAAAAAAAAAAAAALLY REALLY certain that you want to reset to "' + name + '"?. We cannot garuantee your safety from this point.')) {
						window.location.replace("https://www.youtube.com/watch?v=dQw4w9WgXcQ");
					}
				}
			}
		}
	}
	resetCurrentExample(name);
}

function resetCurrentExample(name) {
    loadExampleWorlds();
    currentExample = name;
    currentWorld = ExampleWorlds[currentExample];
	currentArmPosition = [];
    currentArmPosition.push(0);
    currentArmPosition.push(1);
    $('#inputexamples').empty();
    $('#inputexamples').append($('<option value="">').text("(Select an example utterance)"));
    $.each(currentWorld.examples, function(i,value) {
        if (value instanceof Array) value = value.join(" ");
        $('#inputexamples').append($('<option>').text(value));
    });
    $("#dialogue > p").remove();
    resetSVG();
}

function resetSVG() {
    disableInput();
    $("#response").empty();
    sayUtterance("system", "Please wait while I populate the world.");
    $('#svgdiv').empty();

    var viewBox = [0, 0, CanvasWidth + 2 * WallSeparation, CanvasHeight + FloorThickness];
    var svg = $(SVG('svg')).attr({
        viewBox: viewBox.join(' '),
        width: viewBox[2],
        height: viewBox[3],
    }).appendTo($('#svgdiv'));

    // The floor:
    $(SVG('rect')).attr({
        x: 0,
        y: CanvasHeight,
        width: CanvasWidth + 2 * WallSeparation,
        height: CanvasHeight + FloorThickness,
        fill: 'black',
    }).appendTo(svg);

    // The arm:
    $(SVG('line')).attr({
        id:'arm0',
        x1: stackWidth() / 2,
        y1: ArmSize * stackWidth() - CanvasHeight,
        x2: stackWidth() / 2,
        y2: ArmSize * stackWidth(),
        stroke: 'black',
        'stroke-width': ArmSize * stackWidth(),
    }).appendTo(svg);

	$(SVG('line')).attr({
        id:'arm1',
        x1: (stackWidth() / 2) +stackWidth(),
        y1: ArmSize * stackWidth() - CanvasHeight,
        x2: (stackWidth() / 2) +stackWidth(),
        y2: ArmSize * stackWidth(),
        stroke: 'black',
        'stroke-width': ArmSize * stackWidth(),
    }).appendTo(svg);
	
	for(var i = 0;(stackWidth()*i)<CanvasWidth;i++){
		$(SVG('line')).attr({
			id: "asd"+i,
			x1: stackWidth()*i,
			y1: 0,
			x2: stackWidth()*i,
			y2: CanvasHeight,
			stroke: 'black',
			'stroke-width': 1,
		}).appendTo(svg);
	}
	
	
    var timeout = 0;
    for (var stacknr=0; stacknr < currentWorld.world.length; stacknr++) {
        for (var objectnr=0; objectnr < currentWorld.world[stacknr].length; objectnr++) {
            var objectid = currentWorld.world[stacknr][objectnr];
            makeObject(svg, objectid, stacknr, timeout);
            timeout += AnimationPause;
        }
    }
    debugWorld();
    systemPrompt(timeout + PromptPause);
}

function SVG(tag) {
    return document.createElementNS(SvgNS, tag);
}

function animateMotion(object, path, duration, timeout) {
    if (path instanceof Array) {
        path = path.join(" ");
    }
    var animation = SVG('animateMotion');
    $(animation).attr({
        begin: 'indefinite',
        fill: 'freeze',
        path: path,
        dur: duration,
    }).appendTo(object);
	if(timeout){
		animation.beginElementAt(timeout);
	}
    return animation;
}

function moveObject(action, stackNr) {
	console.log(action +  " - "+stackNr)
	if (action == Pick1 && currentWorld.holding1) {
        alertError("ERROR", "I cannot pick an object from stack " + stackNr + ", I am already holding something! 1")
        return 0;
    }  else if (action == Pick2 && currentWorld.holding2) {
        alertError("ERROR", "I cannot pick an object from stack " + stackNr + ", I am already holding something! 2")
        return 0;
    } else if (action == Drop1 && !currentWorld.holding1) {
        alertError("ERROR", "I cannot drop an object onto stack " + stackNr + ", I am not holding anything! 1")
        return 0;
    } else if (action == Drop2 && !currentWorld.holding2) {
        alertError("ERROR", "I cannot drop an object onto stack " + stackNr + ", I am not holding anything! 2")
        return 0;
    }
    var stack = currentWorld.world[stackNr];
	var armint = parseInt(action.substring(4)) -1;
    var arm = $(('#arm'+armint));
    var xStack = stackNr * stackWidth() + WallSeparation;
    var xArm = currentArmPosition[armint] * stackWidth() + WallSeparation;
	
    if (action == Pick1) {
        if (!stack.length) {
            alertError("ERROR", "I cannot pick an object from stack " + stackNr + ", it is empty!")
            return 0;
        }
        currentWorld.holding1 = stack.pop();
    }else if (action == Pick2) {
        if (!stack.length) {
            alertError("ERROR", "I cannot pick an object from stack " + stackNr + ", it is empty!")
            return 0;
        }
        currentWorld.holding2 = stack.pop();
    }

    var altitude = getAltitude(stack);
    var objectHeight = 0;
		if(armint == 0 &&currentWorld.holding1){
			objectHeight = getObjectDimensions(currentWorld.holding1).heightadd;
		}else if(armint == 1 && currentWorld.holding2){
			objectHeight = getObjectDimensions(currentWorld.holding2).heightadd;
		}
	    var yArm = CanvasHeight - altitude - ArmSize * stackWidth() - objectHeight;
    var yStack = -altitude;
	
	var duration1;
	var duration2;
	var anim1 = {t:0};
	var anim2 = {t:0};
	var anim3= {t:0};
	var path1;
	var path2;
	if(armint == 0){
		if(action == Move1){
			path1 = ["M", xArm, 0,"H", xStack,"V",0];
			path2 = ["M", xStack];
			duration1 = (Math.abs(xStack - xArm)) / ArmSpeed;
			duration2 = 0;
		}else{
			path1 = ["M", xArm, 0, "H", xStack, "V", yArm];
			path2 = ["M", xStack, yArm, "V", 0];
			duration1 = (Math.abs(xStack - xArm) + Math.abs(yArm)) / ArmSpeed;
			duration2 = (Math.abs(yArm)) / ArmSpeed;
		}
	}else{
		var xxArm = xArm -stackWidth();
		var xxStack = xStack -stackWidth();
		if(action == Move2){
			path1 = ["M", xxArm, 0, "H", xxStack,"V",0];
			path2 = ["M", xxStack];
			duration1 = (Math.abs(xxStack - xxArm)) / ArmSpeed;
			duration2 = 0;
		}else{		
			path1 = ["M", xxArm, 0, "H", xxStack, "V", yArm];
			path2 = ["M", xxStack, yArm, "V", 0];
			duration1 = (Math.abs(xxStack - xxArm) + Math.abs(yArm)) / ArmSpeed;
			duration2 = (Math.abs(yArm)) / ArmSpeed;
		}
	}
	
	anim1.a = animateMotion(arm, path1, duration1);
	anim2.a = animateMotion(arm, path2, duration2);
	anim2.t = duration1 + AnimationPause;
	
	var hol = armint == 0 ? currentWorld.holding1 : currentWorld.holding2
	
    if (action == Move1 || action == Move2) {
        var path1b = ["M", xArm,yStack-yArm, "H", xStack,"V",0];
        anim3.a = animateMotion($("#"+hol), path1b, duration1)
	}else if (action == Pick1 || action == Pick2) {
        var path2b = ["M", xStack, yStack, "V", yStack-yArm];
        anim3.a = animateMotion($("#"+hol), path2b, duration2)
		anim3.t =duration1 + AnimationPause;
    } else if (action == Drop1 || action == Drop2) {
        var path1b = ["M", xArm, yStack-yArm, "H", xStack, "V", yStack];
        anim3.a = animateMotion($("#"+hol), path1b, duration1)
    }
	
	anim1.a.beginElementAt(anim1.t);
	anim2.a.beginElementAt(anim2.t);
	anim3.a.beginElementAt(anim3.t);
	if(action == Move1){
	//asdas
	}
    if (action == Drop1) {
        stack.push(currentWorld.holding1);
        currentWorld.holding1 = null;
    }else if (action == Drop2) {
        stack.push(currentWorld.holding2);
        currentWorld.holding2 = null;
    }
    currentArmPosition[armint] = stackNr;
    debugWorld();
    return duration1 + duration2 + 2 * AnimationPause;
}

function getObjectDimensions(objectid) {
    var attrs = currentWorld.objects[objectid];
    var size = ObjectData[attrs.form][attrs.size];
    var width = size.width * (stackWidth() - boxSpacing());
    var height = size.height * (stackWidth() - boxSpacing());
    var thickness = size.thickness * (stackWidth() - boxSpacing());
    var heightadd = attrs.form == 'box' ? thickness : height;
    return {
        width: width,
        height: height,
        heightadd: heightadd,
        thickness: thickness,
    };
}

function getAltitude(stack, objectid) {
    var altitude = 0;
    for (var i=0; i<stack.length; i++) {
        if (objectid == stack[i])
            break;
        altitude += getObjectDimensions(stack[i]).heightadd + boxSpacing();
    }
    return altitude;
}

function makeObject(svg, objectid, stacknr, timeout) {
    var attrs = currentWorld.objects[objectid];
    var altitude = getAltitude(currentWorld.world[stacknr], objectid);
    var dim = getObjectDimensions(objectid);

    var ybottom = CanvasHeight - boxSpacing();
    var ytop = ybottom - dim.height;
    var ycenter = (ybottom + ytop) / 2;
    var yradius = (ybottom - ytop) / 2;
    var xleft = (stackWidth() - dim.width) / 2
    var xright = xleft + dim.width;
    var xcenter = (xright + xleft) / 2;
    var xradius = (xright - xleft) / 2;
    var xmidleft = (xcenter + xleft) / 2;
    var xmidright = (xcenter + xright) / 2;

    var object;
    switch (attrs.form) {
    case 'brick':
    case 'plank':
        object = $(SVG('rect')).attr({
            x: xleft,
            y: ytop,
            width: dim.width,
            height: dim.height
        });
        break;
    case 'ball':
        object = $(SVG('ellipse')).attr({
            cx: xcenter,
            cy: ycenter,
            rx: xradius,
            ry: yradius
        });
        break;
    case 'pyramid':
        var points = [xleft, ybottom, xmidleft, ytop, xmidright, ytop, xright, ybottom];
        object = $(SVG('polygon')).attr({
            points: points.join(" ")
        });
        break;
    case 'box':
        var points = [xleft, ytop, xleft, ybottom, xright, ybottom, xright, ytop,
                      xright-dim.thickness, ytop, xright-dim.thickness, ybottom-dim.thickness,
                      xleft+dim.thickness, ybottom-dim.thickness, xleft+dim.thickness, ytop];
        object = $(SVG('polygon')).attr({
            points: points.join(" ")
        });
        break;
    case 'table':
        var points = [xleft, ytop, xright, ytop, xright, ytop+dim.thickness,
                      xmidright, ytop+dim.thickness, xmidright, ybottom,
                      xmidright-dim.thickness, ybottom, xmidright-dim.thickness, ytop+dim.thickness,
                      xmidleft+dim.thickness, ytop+dim.thickness, xmidleft+dim.thickness, ybottom,
                      xmidleft, ybottom, xmidleft, ytop+dim.thickness, xleft, ytop+dim.thickness];
        object = $(SVG('polygon')).attr({
            points: points.join(" ")
        });
        break;
    }
    object.attr({
        id: objectid,
        stroke: 'black',
        'stroke-width': boxSpacing() / 2,
        fill: attrs.color,
    });
    object.appendTo(svg);

    var path = ["M", stacknr * stackWidth() + WallSeparation, -(CanvasHeight + FloorThickness)];
    animateMotion(object, path, 0, 0).beginElementAt(0);
    path.push("V", -altitude);
    animateMotion(object, path, 0.5).beginElementAt(timeout);
}

function disableInput(timeout) {
    if (timeout) {
        setTimeout(disableInput, 1000*timeout);
    } else {
        $("#inputexamples").blur();
        $("#inputexamples").prop('disabled', true);
        $("#userinput").blur();
        $("#userinput").prop('disabled', true);
    }
}

function systemPrompt(timeout) {
    if (timeout) {
        setTimeout(systemPrompt, 1000*timeout);
    } else {
        sayUtterance("system", SystemPromptText);
        enableInput();
    }
}

function enableInput() {
    $("#inputexamples").prop('disabled', false).val('');
    $("#inputexamples option:first").attr('selected','selected');
    $("#userinput").prop('disabled', false);
    $("#userinput").focus().select();
}

function performPlan() {
    if (currentPlan && currentPlan.length > 1) {
		var item1 = currentPlan.shift();
		var item2 = currentPlan.shift();
        var timeout = 0;
		var action1 = getAction(item1);
		var action2 = getAction(item2);
		if (action1 && action2) {
			var timeout1 = 0;
			var timeout2 = 0;
			if(action1){
				timeout1 = moveObject(action1[0], action1[1]);
			}
			if(action2){
				timeout2 = moveObject(action2[0], action2[1]);
			}
			timeout = Math.max(timeout1, timeout2);
        } else {
			sayUtterance("system", item1);
			sayUtterance("system", item2);
        }
        setTimeout(performPlan, 1000 * timeout);
    } else {
        systemPrompt(PromptPause);
    }
}

function getAction(item) {
    if (typeof(item) == "string") item = item.trim().split(/\s+/);
    if (item.length == 2 &&
        (item[0] == Pick1 || item[0] == Drop1 || item[0] == Move1 || item[0] == Pick2 || item[0] == Drop2 || item[0] == Move2) &&
        /^\d+$/.test(item[1]))
    {
        item[1] = parseInt(item[1]);
        return item;
    }
    return null;
}

function splitAction(action) {
}

function userInput() {
    var userinput = $("#inputexamples").val();
    if (userinput) {
        $("#userinput").val(userinput.trim());
        enableInput();
        return;
    }
    userinput = $("#userinput").val().trim();
    if (!userinput) {
        enableInput();
        return;
    }
    disableInput();

    sayUtterance("user", userinput);



    var ajaxdata = {'world': currentWorld.world,
                    'objects': currentWorld.objects,
                    'holding': currentWorld.holding,
                    'state': currentWorld.state,
                    'utterance': userinput.split(/\s+/),
					'question' : currentQuestion,
                   };
	if(currentQuestion){
		ajaxdata.question.answer = userinput;
		if(lastUtterance){
			ajaxdata.utterance = lastUtterance;
			lastUtterance = null;
		}
	}else{
		lastUtterance = ajaxdata.utterance;
	}


    $.ajax({
        url: AjaxScript,
        dataType: "text",
        cache: false,
        timeout: 1000 * AjaxTimeout,
        data: {'data': JSON.stringify(ajaxdata)}
    }).fail(function(jqxhr, status, error) {
        alertError("Internal error: " + status, error);
        systemPrompt();
    }).done(function(result) {
        try {
			currentQuestion = null;
            result = JSON.parse(result);
			if(result.question){
				console.log(result.question);
				sayUtterance("system", "A question from the server, did you mean: ");
				var keys = Object.keys(result.question.questions);
				for(var i = 0 ;i<keys.length;i++){
					sayUtterance("system", keys[i] +" - " + result.question.questions[keys[i]]);
				}
				currentPlan = null;
				currentQuestion = result.question;
				enableInput();
			}else{
				debugResult(result);
				sayUtterance("system", result.output);
				if (result.state) {
					currentWorld.state = result.state;
				}
				currentPlan = result.plan;
				currentQuestion = null;
				performPlan();
			}
        } catch(err) {
			console.log(result);
            alertError("JSON error222:" + err, result);
        }


    });
}

function sayUtterance(participant, utterance, silent) {
    var dialogue = $("#dialogue");
    if (dialogue.children().length > DialogueHistory) {
        dialogue.children().first().remove();
    }
    $('<p>').attr("class", participant)
        .text(utterance)
        .insertBefore($("#inputform"));
    dialogue.scrollTop(dialogue.prop("scrollHeight"));
    if (UseSpeech && !silent) {
        try {
            // W3C Speech API (works in Chrome and Safari)
            var speech = new SpeechSynthesisUtterance(utterance);
            for (var attr in Voices[participant]) {
                speech[attr] = Voices[participant][attr];
            }
            console.log("speakING: " + utterance);
            //window.speechSynthesis.speak(speech);
        } catch(err) {
        }
    }
}

function debugWorld() {
    $("#debugworld").html("<table><tr><td>&nbsp;" + currentWorld.world.join("&nbsp;<td>&nbsp;") + "&nbsp;</tr></table>");
    $("#debugholding").html(currentWorld.holding || "&mdash;");
}

function debugResult(result) {
    $("#debugoutput").text(result.output);
    $("#debugtrees").html(result.trees ? result.trees.join("<br>") : "&mdash;");
    $("#debuggoals").html(result.goals ? result.goals.join("<br>") : "&mdash;");
    $("#debugplan").html(result.plan ? result.plan.join("<br>") : "&mdash;");
    $("#debugstate").html(result.state ? JSON.stringify(result.state) : "&mdash;");
    $("#debugjson").text(JSON.stringify(result, null, " "));
}

function alertError(title, description) {
    if (typeof(description) !== "string") description = JSON.stringify(description);
    sayUtterance("error", "[" + title + "] " + description, true);
    console.log("*** " + title + " ***");
    console.log(description);
}
