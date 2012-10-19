	
function Multi(canvas) {
			var proc = new Processing(canvas);
			proc.multi = this;
			//Multi.prototype.constructor=Multi; 
			//this = new Processing(canvas);

			//static char this.CONFIG_T = 'C';
			proc.numbers;
			proc.bigNum;
			proc.medNum;
			this.faceX = 260;
			this.faceY = 260;
			this.dia = 400;
			this.f = proc.color(0);

			// The serial port:
			//Serial nmeaPort;  
			//static int LF = 10; //line feed
			//Compass compass;
			this.currentHeading = 0;
			this.lat = 0.0;
			this.latStr = "0.0000";
			this.lon = 0.0;
			this.lonStr = "0.0000";
			this.apparentWindDir = 90;
			this.apparentWindSpeed = 6;
			this.trueWindDir = 0;
			this.trueWindSpeed = 0;
			this.speed = 6;
			this.closeHauled = 45;
			this.config = new Configuration();

			this.setup = function() {
				proc.size(520, 590);
				
				proc.smooth();
				proc.rectMode(proc.CENTER);
				proc.frameRate(10);

				//this.numbers = proc.loadFont("TheSans-Plain-12.vlw");
				proc.numbers = proc.createFont("sans-serif", 24);
				proc.bigNum = proc.createFont("sans-serif", 48);
				proc.medNum = proc.createFont("sans-serif", 36);

				//setup serial input
				//nmeaPort=new Serial(this, "/dev/ttyUSB0", 38400);    

				proc.noStroke();
				this.compass = new Compass(this.faceX, this.faceY);
				this.compass.setup();
			}

			proc.draw =  function(){

				//Gauge 
				proc.smooth();
				proc.background(255, 204, 0);
				
				multi.drawCloseHauled();
				multi.compass.update();
				multi.compass.draw();
				
				//apparent wind vector
				multi.drawApparentWind();
				multi.drawTrueWind();
				multi.drawHeading();
				multi.drawBoat();
				multi.drawSpeed();
				multi.drawLat();
				multi.drawLon();

			}

			this.drawBoat = function() {
				//draw boat
				proc.fill(255);
				proc.rect(this.faceX, this.faceY - 60, 2, this.dia / 2);//0 deg
				proc.quad(this.faceX, this.faceY + 10, this.faceX - 6, this.faceY, this.faceX,
						this.faceY - 15, this.faceX + 6, this.faceY);
			}
			this.drawCloseHauled = function() {
				proc.fill(this.f);
				proc.ellipse(this.faceX, this.faceY, this.dia, this.dia);
				proc.fill(119, 136, 153);
				proc.arc(this.faceX, this.faceY, this.dia, this.dia, proc.radians(-90
						- this.closeHauled), proc.radians(-90 + this.closeHauled));
			}

			this.drawSpeed = function() {
				proc.translate(0, 0);
				proc.fill(this.f);
				proc.textFont(proc.numbers);
				proc.text("KNTS", this.faceX * .25, this.faceX * .2);
				proc.textFont(proc.bigNum);
				proc.textAlign(proc.CENTER);
				proc.text(proc.str(Math.round(this.speed, 2)), this.faceX * .3,
						this.faceX * .35);
				proc.textAlign(proc.LEFT);
				//speed vector
				if (this.speed > 0) {
					proc.pushMatrix();
					proc.noFill();
					proc.strokeWeight(4);
					proc.stroke(0, 255, 0);
					proc.translate(this.faceX, this.faceY);
					var sp = 10 + this.speed * (this.faceX * .05);
					sp = proc.constrain(sp, 0, 100);
					proc.rect(0, -sp / 2, 16, sp);
					proc.quad(0, -sp, -12, -sp, 0, -sp - 16, 12, -sp);
					proc.noStroke();
					proc.popMatrix();
				}
			}

			this.drawHeading = function() {
				proc.translate(0, 0);
				proc.fill(this.f);
				proc.textFont(proc.numbers);
				proc.text("degT", this.faceX * 1.5, this.faceX * .2);
				proc.textFont(proc.bigNum);
				proc.textAlign(proc.CENTER);
				proc.text(proc.str(Math.round(this.compass.lastHeading)), this.faceX * 1.55,
						this.faceX * .35);
				proc.textAlign(proc.LEFT);
			}

			this.drawLat = function() {
				proc.translate(0, 0);
				proc.fill(this.f);
				proc.textFont(proc.numbers);
				proc.text("LAT", this.faceX * .25, this.faceX * 2);
				proc.textFont(proc.bigNum);
				proc.text(this.latStr, this.faceX * .5, this.faceX * 2);
			}
			this.drawLon = function() {
				proc.translate(0, 0);
				proc.fill(this.f);
				proc.textFont(proc.numbers);
				proc.text("LON", this.faceX * .25, this.faceY * 2.2);
				proc.textFont(proc.bigNum);
				proc.text(this.lonStr, this.faceX * .5, this.faceY * 2.2);
			}

			this.drawApparentWind = function() {
				//apparent wind vector in degrees 0-360, clockwise off the bow.
				if (this.apparentWindSpeed > 0) {
					proc.pushMatrix();
					proc.translate(this.faceX, this.faceY);
					//+ve = clockwise
					proc.rotate(proc.radians(-this.apparentWindDir));
					proc.fill(255, 0, 0);
					var len = 14 + this.apparentWindSpeed * (this.faceX * .015);
					len = proc.constrain(len, 0, this.faceX * .8);
					proc.strokeWeight(4);
					proc.rect(0, -(len / 2), 8, len);
					proc.rect(0, 15, 8, 25);
					proc.quad(0, -len + 12, -12, -len - 20, 0, -len - 16, 12,
							-len - 20);
					proc.pushMatrix();
					proc.translate(0, 50);
					proc.rotate(proc.radians(this.apparentWindDir));
					proc.textFont(proc.medNum);
					proc.text(proc.str(Math.round(this.apparentWindSpeed)), -6, 0);
					proc.popMatrix();
					proc.popMatrix();
				}
			}
			this.drawTrueWind = function() {
				//true wind vector
				this.trueWindSpeed = this.calcTrueWindSpeed(this.apparentWindSpeed,
						this.apparentWindDir, this.speed);
				this.trueWindSpeed = proc.constrain(this.trueWindSpeed, 0, 200);

				this.trueWindDir = this.trueWindDirection(this.apparentWindSpeed,
						this.apparentWindDir, this.speed);
				//trueWindDir=constrain(trueWindDir,0,360);

				if (this.trueWindSpeed > 0) {
					proc.pushMatrix();
					proc.translate(this.faceX, this.faceY);
					proc.rotate(proc.radians(-this.trueWindDir));
					proc.noFill();
					proc.strokeWeight(4);
					proc.stroke(255, 204, 0);
					var len = 14 + this.trueWindSpeed * (this.faceX * .015);
					len = proc.constrain(len, 0, this.faceX * .8);
					proc.rect(0, -(len / 2), 12, len);
					proc.rect(0, 15, 12, 25);
					proc.quad(0, -len + 12, -12, -len - 20, 0, -len - 16, 12, -len - 20);
					proc.noStroke();
					proc.pushMatrix();
					proc.translate(0, 70);
					proc.rotate(proc.radians(this.trueWindDir));
					proc.fill(255, 204, 0);
					proc.textFont(proc.medNum);
					proc.text(proc.str(Math.round(this.trueWindSpeed)), -6, 0);
					proc.popMatrix();
					proc.popMatrix();
				}
			}

			/**
			 * Calculates the true wind direction from apparent wind on vessel
			 * Result is relative to bow
			 * 
			 * @param apparentWind
			 * @param apparentDirection 0 to 360 deg to the bow
			 * @param vesselSpeed
			 * @return
			 */
			this.trueWindDirection = function(apparentWind, apparentDirection,
					vesselSpeed) {
				/*
					 Y = 90 - D
					a = AW * ( cos Y )
					bb = AW * ( sin Y )
					b = bb - BS
					True-Wind Speed = (( a * a ) + ( b * b )) 1/2
					True-Wind Angle = 90-arctangent ( b / a )
				 */
				apparentDirection = apparentDirection % 360;
				var stbd = apparentDirection < 180;
				if (!stbd) {
					apparentDirection = 360 - apparentDirection;
				}
				var y = 90 - apparentDirection;
				var a = apparentWind * proc.cos(proc.radians(y));
				var b = (apparentWind * proc.sin(proc.radians(y))) - vesselSpeed;
				var td = 90 - proc.degrees(proc.atan((b / a)));
				if (!stbd)
					return 360 - td;
				return td;

			}

			/**
			 * Calculates the true wind speed from apparent wind speed on vessel
			 * 
			 * @param apparentWind
			 * @param apparentDirection 0 to 360 deg to the bow
			 * @param vesselSpeed
			 * @return
			 */
			this.calcTrueWindSpeed = function(apparentWind, apparentDirection,
					vesselSpeed) {
				apparentDirection = apparentDirection % 360;
				if (apparentDirection > 180) {
					apparentDirection = 360 - apparentDirection;
				}
				var y = 90 - apparentDirection;
				var a = apparentWind * proc.cos(proc.radians(y));
				var b = (apparentWind * proc.sin(proc.radians(y))) - vesselSpeed;
				return (proc.sqrt((a * a) + (b * b)));
			}
			

			function Compass(faceX,faceY) {
				this.faceX=faceX;
				this.faceY=faceY;
				this.s1 = 190; //compass ring radius
				this.s2 = 20; //length of ticks
				this.s3 = 4; 
				this.s5 = 155; //compass deg text radius
				this.s6 = -20; //move deg text right
				this.s7 = 10; //move deg text down
				this.lastHeading = 0;
				this.newHeading = 0;
				this.t = new Tween(this, "lastHeading", Tween.strongEaseInOut,
						this.lastHeading, this.newHeading, 2);

				this.setup = function() {
				}

				this.setCurrentHeading = function(heading) {
					this.newHeading = heading;
					this.t = new Tween(this, "lastHeading",
							Tween.strongEaseInOut, this.lastHeading,
							this.newHeading, 2);
					this.t.start();
				}

				this.update = function() {
					this.t.tick();
				}

				this.draw = function() {
					this.doDraw(this.lastHeading);
				}

				this.doDraw = function(heading) {

					proc.pushMatrix();
					proc.translate(this.faceX, this.faceY);
					//this.rotate(this.radians(-90));
					proc.rotate(proc.radians(-90 - heading));

					//Compass Dashes 
					//rect(130, 0, 10, 3);//0 deg
					var raised = 0; //0 deg
					while (raised <= 360) {
						proc.fill(255);
						proc.pushMatrix();
						//translate(faceX,faceY);
						proc.rotate(proc.radians(raised));
						//this.rect(120, 0, 10, 2);
						proc.rect(this.s1, 0, this.s2, this.s3);
						proc.pushMatrix();
						//this.translate(105,0);
						proc.translate(this.s5, 0);
						proc.rotate(proc.radians(-raised + 90 + heading));
						proc.textFont(proc.numbers);
						proc.fill(255);
						if (raised < 360) {
							if (raised == 0) {
								proc.text("N", this.s6, this.s7);
							} else {
								proc.text(proc.str(raised), this.s6, this.s7);
							}
						}
						proc.popMatrix();
						proc.popMatrix();
						raised = raised + 90;
					}
					raised = 45; //45 deg
					while (raised <= 360) {
						proc.fill(255);
						proc.pushMatrix();
						//translate(faceX,faceY);
						proc.rotate(proc.radians(raised));
						//this.rect(120, 0, 10, 1);
						proc.rect(this.s1, 0, this.s2, this.s3 / 2);
						proc.pushMatrix();
						//this.translate(105,0);
						proc.translate(this.s5, 0);
						proc.rotate(proc.radians(-raised + 90 + heading));
						proc.textFont(proc.numbers);
						proc.fill(255);
						if (raised < 360) {
							proc.text(proc.str(raised), this.s6, this.s7);
						}
						proc.popMatrix();
						proc.popMatrix();
						raised = raised + 90;
					}
					raised = 10; //10 deg
					while (raised <= 360) {
						proc.fill(255);
						proc.pushMatrix();
						//translate(faceX,faceY);
						proc.rotate(proc.radians(raised));
						//this.rect(120, 0, 5, 1);
						proc.rect(this.s1, 0, this.s2, this.s3 / 2);
						proc.popMatrix();
						raised = raised + 10;
					}
					raised = 2; //2deg
					while (raised <= 360) {
						proc.fill(255);
						proc.pushMatrix();
						//translate(faceX,faceY);
						proc.rotate(proc.radians(raised));
						//this.rect(120, 0, 2, 1);
						proc.rect(this.s1, 0, this.s2, this.s3 / 2);
						proc.popMatrix();
						raised = raised + 2;
					}
					proc.popMatrix();
					
				}

			}

			function Configuration() {
				this.anchorLat = 0.0;
				this.anchorLon = 0.0;
				this.anchorRadius = 40.0;
				this.anchorAlarmOn = false;
				this.autopilotOn = false;
				this.autopilotAlarmOn = false;
				this.gpsSpeedUnit = 1.0;
				this.gpsAlarmOn = false;
				this.gpsAlarmFixTime = 1000 * 60 * 5; //5 min
				this.radarAlarmOn = false;
				this.mobAlarmOn = false;
				this.windAlarmSpeed = 99;
				this.windAlarmOn = false;
				this.windFactor = 2200000;
				this.windZeroOffset = 50;

			}
			
}