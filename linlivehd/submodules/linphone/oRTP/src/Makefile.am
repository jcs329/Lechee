
EXTRA_DIST=dll_entry.c master system winrttimer.cpp winrttimer.h

AM_CPPFLAGS=\
	$(STRICT_OPTIONS) \
	$(STRICT_OPTIONS_CXX) \
	-I$(top_srcdir)/include/ \
	-I$(top_srcdir)

AM_CFLAGS=\
	$(STRICT_OPTIONS) \
	$(STRICT_OPTIONS_CC) \
	$(PTHREAD_CFLAGS) \
	$(TRUESPEECH_CFLAGS) \
	$(SRTP_CFLAGS)

AM_LDFLAGS=$(PTHREAD_LDFLAGS)

lib_LTLIBRARIES = libortp.la

libortp_la_SOURCES=	str_utils.c 	\
			port.c \
			logging.c \
			rtpparse.c  \
			rtpsession.c \
			rtpsession_inet.c \
			rtpsession_priv.h \
			jitterctl.c jitterctl.h \
			rtpsignaltable.c  \
			rtptimer.c	rtptimer.h \
			posixtimer.c \
			ortp.c \
			scheduler.c scheduler.h \
			avprofile.c  \
			sessionset.c  \
			telephonyevents.c  \
			payloadtype.c \
			rtpprofile.c \
			rtcp.c \
			rtcp_fb.c \
			rtcp_xr.c \
			utils.c utils.h \
			rtcpparse.c \
			event.c \
			stun.c stun_udp.c \
			ortp_srtp.c \
			b64.c \
			zrtp.c \
			netsim.c

if LIBBZRTP
AM_CFLAGS+= $(LIBBZRTP_CFLAGS)
AM_LDFLAGS+= $(LIBBZRTP_LDFLAGS)
endif

libortp_la_LIBADD= $(PTHREAD_LIBS) $(RT_LIBS) -lm $(SRTP_LIBS) $(SSL_LIBS) $(LIBBZRTP_LIBS)

libortp_la_LDFLAGS= -version-info $(LIBORTP_SO_VERSION) -no-undefined

if HAVE_LD_OUTPUT_DEF
libortp_la_LDFLAGS += -Wl,--output-def,libortp-$(LIBORTP_SO_CURRENT).def
defexecdir = $(libdir)
defexec_DATA = libortp-$(LIBORTP_SO_CURRENT).def
CLEANFILES = $(defexec_DATA)

libortp-$(LIBORTP_SO_CURRENT).def: libortp.la

if BUILD_WIN32
defexec_DATA += libortp-$(LIBORTP_SO_CURRENT).lib
libortp-$(LIBORTP_SO_CURRENT).lib: libortp-$(LIBORTP_SO_CURRENT).def libortp.la
	$(DLLTOOL) --dllname libortp-$(LIBORTP_SO_CURRENT).dll --input-def libortp-$(LIBORTP_SO_CURRENT).def --output-lib $@ libortp.la
endif
endif

SUBDIRS= . tests
