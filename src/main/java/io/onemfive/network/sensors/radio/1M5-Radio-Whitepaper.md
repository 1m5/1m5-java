# 1M5 Radio - Uncensored communications over the radio spectrum (3/30Hz to 300/3000GHz)

## Abstract
[You may be tempted to put your conclusion at the end, but a white paper needs to grab the reader immediately. 
Include direct, pithy statements of your position to engage the reader. Although this section provides a short summary 
of what the paper is about, you need to provide enough detail to satisfy a busy executive while encouraging the reader 
to continue on to the meat of the paper.]

## Introduction
[Define the issue and provide background discussion while building credibility. Find common ground with your audience 
and hook them in.]

Internet access is completely controlled by telecommunication backbone providers, largely cellular organizations like Verizon and AT&T in the
US and other large telecommunications organizations and/or governments globally in their respective jurisdictions. When
governments and/or corporations decide an individual needs to be blocked, it's relatively easy to prevent a person access
unless they go to public access points or use a friend or family's access. If that person is considered a felon by government,
aiding that person could result in becoming a felon themselves. Losing access to the internet today can be debilitating
as so much of our lives is dependent upon using it. And thus self-censorship ensures. How can we communicate over long 
distances or even just down the block electronically ensuring censorship resistance when we have been cut off from 
the internet? Create our own radio network!

## Opportunities
[Don't start selling yet. Thoroughly and completely identify the business problem your technology solves. 
This section should be entirely from the perspective of the target audience.]

Creating our own network is likely to require trade-offs. In more developed areas of the world, we are used to high-bandwidth
low-latency communications due to the massive investment in the global fiber-optic backbone by governments and primary
carriers. Developing our own networks outside of this, while greatly enhancing privacy and availability to blocked users,
will highly likely result in the need to use low-bandwidth higher-latency networking technologies as the investment is
not there. This is similar to the issue with Bitcoin vs Fiat. We are used to instant transactions using fiat while Bitcoin
normally takes 10-30 minutes and can take upwards of days when the network is under great strain. 
 
## Solution
[Describe relevant technologies at a high level, including any competing technologies (which you will rebut later). 
Support your arguments with tables, charts, and graphics. Quote industry experts as needed to bolster your positions. 
You are educating your target audience on the current state of the art, as well as where your solution fits.] 

This 1M5 Radio component will act as a sensor within the 1M5 stack to continue communications when the internet is blocked
or just no longer available. Asynchronous messaging like email will be the first applications of this new network then 
moving into messaging, content publishing, and then voice and video streaming once more of these decentralized 
technologies have matured.

## Details
[Having thoroughly explained the problem and the general approach to solving it, it is finally time to describe your 
solution in more detail. You can start selling know, but avoid grandiose claims; the soft approach works better in white papers. 
Be sure to show how your solution is vastly superior to the competition. Remember your audience and use appropriate 
language and level of detail. This is the heart of your white paper and you'll probably want to use case studies or 
customer testimonials to support your arguments.]
Upon startup, the Radio Sensor loads the radio-sensor.config file to configure itself instantiating an instance of the 
radio library to be used, e.g. GNU Radio. 

## Benefits
[This section is the soul of your white paper, where you need to grab the reader where he or she lives and provide 
plenty of assurances that your solution will work for them. Discuss return on investment (ROI), usability, adherence 
to standards, and speed of implementation. Show you understand your readers' pain and can relieve it. Customer quotes 
again may be helpful.]

## Roadmap
* 0.1.0 2000 person hours @ 30 BTC: Prove messages can be sent between two relatively cheap computers that could be used as a cheap mobile phone (e.g. Raspberry Pi) using a software defined radio (SDR) over shortwave.
* 0.2.0 500 person hours @ 7.5 BTC: Integrate Sensor into Sensor Service providing Discovery of Radio Peers
* 0.3.0 500 person hours @ 7.5 BTC: Integrate Sensor into Sensor Manager supporting re-routing of messages when the internet is blocked or unavailable.
* 0.4.0 500 person hours @ 7.5 BTC: Provide automated band selection and tuning
* 0.5.0 2000 person hours @ 30 BTC: Determine and support the widest possible radio spectrum
* 0.6.0 2000 person hours @ 40 BTC: Detect Jamming
* 0.7.0 2000 person hours @ 40 BTC: Provide strategies for defeating jamming attempts
* 0.8.0 2000 person hours @ 30 BTC: Development strategies for ensuring anonymity
* 0.9.0 1000 person hours @ 15 BTC: Maximize availability
* 1.0.0 2000 person hours @ 40 BTC: Support ECM for defensive purposes
 
## Summary
[A quick summary emphasizes both the benefits of your solution as well as the risks to readers who decide not to use 
your product or service. Many readers may skip the entire document and read this section only, so write this section as 
if it were an entirely standalone document summarizing the main selling points about your solution. Conclude with the 
most important point that you want the reader to remember.]

## Call to Action
[You'd be surprised how many white papers either leave this most important section out or fumble it at the end. 
Emphatically tell readers what you want them to do and how to do it. We're not talking about simply dropping in a 
contact person's name and telephone number as well as Website, email, and snail mail addresses. We prefer white papers 
that end with offers — free trial version, free assessment, free gift certificate if you call today, that sort of thing.]